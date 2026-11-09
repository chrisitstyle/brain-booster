"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";

import {
  BrainCircuit,
  Check,
  CheckCircle2,
  Eye,
  Loader2,
  RefreshCw,
  RotateCcw,
  Zap,
} from "lucide-react";

import StudyFlashcardCard from "@/app/users/[nickname]/sets/components/study-flashcard-card";
import {
  getReviewQueue,
  submitFlashcardReview,
} from "@/api/spacedRepetitionService";
import { ApiError } from "@/api/apiError";
import { Button } from "@/components/ui/button";
import GamePageLayout from "@/components/games/shared/GamePageLayout";
import GameProgress from "@/components/games/shared/GameProgress";
import GameShell from "@/components/games/shared/GameShell";
import { useAuth } from "@/context/AuthContext";

import type {
  ReviewCard,
  ReviewRating,
  SubmitReviewRequest,
} from "@/types/spaced-repetition";

interface ReviewSessionClientProps {
  flashcardSetId: number;
  ownerNickname: string;
}

type QueueStatus = "loading" | "ready" | "error";

const reviewRatingOptions = [
  {
    rating: "AGAIN",
    label: "Again",
    description: "I forgot",
    icon: RotateCcw,
    className:
      "border-red-200 text-red-600 hover:bg-red-50 dark:border-red-900 dark:text-red-400 dark:hover:bg-red-950/40",
  },
  {
    rating: "HARD",
    label: "Hard",
    description: "Difficult",
    icon: BrainCircuit,
    className:
      "border-amber-200 text-amber-700 hover:bg-amber-50 dark:border-amber-900 dark:text-amber-400 dark:hover:bg-amber-950/40",
  },
  {
    rating: "GOOD",
    label: "Good",
    description: "Remembered",
    icon: Check,
    className:
      "border-green-200 text-green-700 hover:bg-green-50 dark:border-green-900 dark:text-green-400 dark:hover:bg-green-950/40",
  },
  {
    rating: "EASY",
    label: "Easy",
    description: "Effortless",
    icon: Zap,
    className:
      "border-pink-200 text-pink-600 hover:bg-pink-50 dark:border-pink-900 dark:text-pink-400 dark:hover:bg-pink-950/40",
  },
] as const;

/**
 * Manages an authenticated FSRS review session for a flashcard set.
 *
 * <p>Each rating is persisted before advancing to the next flashcard.
 * A pending submission retains its original request ID so ambiguous
 * network failures can be retried without applying a rating twice.
 *
 * <p>The review queue and scheduling state are managed by the backend.
 */
export default function ReviewSessionClient({
  flashcardSetId,
  ownerNickname,
}: ReviewSessionClientProps) {
  const { token, isAuthLoading } = useAuth();

  const [reviewQueue, setReviewQueue] = useState<ReviewCard[]>([]);
  const [queueStatus, setQueueStatus] = useState<QueueStatus>("loading");
  const [queueError, setQueueError] = useState<string | null>(null);

  const [currentCardIndex, setCurrentCardIndex] = useState(0);
  const [isFlipped, setIsFlipped] = useState(false);

  const [completedRatings, setCompletedRatings] = useState<ReviewRating[]>([]);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submissionError, setSubmissionError] = useState<string | null>(null);

  const [requiresQueueRefresh, setRequiresQueueRefresh] = useState(false);

  const [pendingReviewRequest, setPendingReviewRequest] =
    useState<SubmitReviewRequest | null>(null);

  const [reloadCounter, setReloadCounter] = useState(0);

  // Refs synchronously prevent duplicate submissions.
  const pendingSubmissionRef = useRef<SubmitReviewRequest | null>(null);

  const isSubmittingRef = useRef(false);

  /**
   * Loads the current review queue for the authenticated user.
   *
   * Outdated requests are cancelled when the selected set,
   * authentication state or reload counter changes.
   */
  useEffect(() => {
    if (isAuthLoading || !token) {
      return;
    }

    const abortController = new AbortController();

    setQueueStatus("loading");
    setQueueError(null);

    async function loadAvailableReviews(authenticatedToken: string) {
      try {
        const availableCards = await getReviewQueue(
          flashcardSetId,
          authenticatedToken,
          abortController.signal,
        );

        if (abortController.signal.aborted) {
          return;
        }

        setReviewQueue(availableCards);
        setCurrentCardIndex(0);
        setCompletedRatings([]);
        setIsFlipped(false);

        setSubmissionError(null);
        setRequiresQueueRefresh(false);

        pendingSubmissionRef.current = null;
        setPendingReviewRequest(null);

        setQueueStatus("ready");
      } catch (error: unknown) {
        if (abortController.signal.aborted) {
          return;
        }

        setQueueError(
          getReviewErrorMessage(error, "Failed to load flashcard reviews."),
        );

        setQueueStatus("error");
      }
    }

    void loadAvailableReviews(token);

    return () => {
      abortController.abort();
    };
  }, [flashcardSetId, isAuthLoading, token, reloadCounter]);

  /**
   * Stops speech playback when the current flashcard changes
   * or the review page is unmounted.
   */
  useEffect(() => {
    return () => {
      if ("speechSynthesis" in window) {
        window.speechSynthesis.cancel();
      }
    };
  }, [currentCardIndex]);

  const currentReviewCard = reviewQueue[currentCardIndex];

  // Adapt the review DTO to the existing study flashcard component.
  // The review queue does not include the starred property.
  const displayedFlashcard = currentReviewCard
    ? {
        flashcardId: currentReviewCard.flashcardId,
        setId: flashcardSetId,
        term: currentReviewCard.term,
        definition: currentReviewCard.definition,
        starred: false,
      }
    : undefined;

  const isSessionComplete =
    queueStatus === "ready" && currentCardIndex >= reviewQueue.length;

  /**
   * Requests a fresh queue only when no review submission
   * is currently running or awaiting confirmation.
   */
  function reloadReviewQueue() {
    if (isSubmittingRef.current || pendingSubmissionRef.current !== null) {
      return;
    }

    setReloadCounter((previousCounter) => previousCounter + 1);
  }

  /**
   * Reads the selected flashcard content aloud.
   */
  function speakFlashcardText(text: string) {
    if (!("speechSynthesis" in window)) {
      return;
    }

    window.speechSynthesis.cancel();

    const utterance = new SpeechSynthesisUtterance(text);

    window.speechSynthesis.speak(utterance);
  }

  /**
   * Persists a review and advances only after successful confirmation.
   *
   * Ambiguous failures preserve the request ID. Known validation
   * conflicts require reloading the review queue.
   */
  async function saveReview(reviewRequest: SubmitReviewRequest) {
    if (isSubmittingRef.current || !token) {
      return;
    }

    isSubmittingRef.current = true;

    setIsSubmitting(true);
    setSubmissionError(null);

    try {
      await submitFlashcardReview(reviewRequest, token);

      // Advance only after the backend confirms persistence.
      setCompletedRatings((previousRatings) => [
        ...previousRatings,
        reviewRequest.rating,
      ]);

      setCurrentCardIndex((previousIndex) => previousIndex + 1);

      setIsFlipped(false);

      pendingSubmissionRef.current = null;
      setPendingReviewRequest(null);

      setRequiresQueueRefresh(false);
    } catch (error: unknown) {
      const isKnownApiConflict =
        error instanceof ApiError &&
        [400, 401, 403, 404, 409].includes(error.status);

      if (isKnownApiConflict) {
        // The rejected request must not be retried unchanged.
        // Reload the queue to obtain the current backend state.
        pendingSubmissionRef.current = null;
        setPendingReviewRequest(null);
        setRequiresQueueRefresh(true);
      }

      // For network failures or unknown server errors, keep
      // the original request ID for an idempotent retry.
      setSubmissionError(
        getReviewErrorMessage(
          error,
          "Failed to save your review. Please try again.",
        ),
      );
    } finally {
      isSubmittingRef.current = false;
      setIsSubmitting(false);
    }
  }

  /**
   * Creates a unique logical request for the selected rating.
   *
   * A synchronous guard prevents multiple clicks from generating
   * separate request IDs for the same flashcard.
   */
  function handleRatingSelected(reviewRating: ReviewRating) {
    if (
      !currentReviewCard ||
      !isFlipped ||
      isSubmittingRef.current ||
      requiresQueueRefresh ||
      pendingSubmissionRef.current !== null
    ) {
      return;
    }

    const reviewRequest: SubmitReviewRequest = {
      flashcardId: currentReviewCard.flashcardId,
      rating: reviewRating,
      requestId: crypto.randomUUID(),
    };

    // Preserve the exact submission for any necessary retry.
    pendingSubmissionRef.current = reviewRequest;
    setPendingReviewRequest(reviewRequest);

    void saveReview(reviewRequest);
  }

  /**
   * Retries the original review without generating another request ID.
   */
  function retryPendingReview() {
    const pendingReview = pendingSubmissionRef.current;

    if (!pendingReview || isSubmittingRef.current) {
      return;
    }

    void saveReview(pendingReview);
  }

  /**
   * Prevents flipping while the submitted rating is unresolved.
   */
  function handleFlashcardFlip() {
    if (
      isSubmittingRef.current ||
      pendingSubmissionRef.current !== null ||
      requiresQueueRefresh
    ) {
      return;
    }

    setIsFlipped((previousValue) => !previousValue);
  }

  const reviewContent = (() => {
    if (isAuthLoading) {
      return <ReviewStatusMessage message="Checking authentication..." />;
    }

    if (!token) {
      return (
        <GameShell>
          <div className="space-y-5 py-8 text-center">
            <p className="text-muted-foreground">
              Sign in to review your flashcards.
            </p>

            <Button
              asChild
              className="bg-pink-500 text-white hover:bg-pink-600"
            >
              <Link href="/login">Sign in</Link>
            </Button>
          </div>
        </GameShell>
      );
    }

    if (queueStatus === "loading") {
      return <ReviewStatusMessage message="Loading your reviews..." />;
    }

    if (queueStatus === "error") {
      return (
        <GameShell>
          <div className="space-y-4 py-8 text-center">
            <p role="alert" className="text-sm text-destructive">
              {queueError}
            </p>

            <Button type="button" variant="outline" onClick={reloadReviewQueue}>
              <RefreshCw className="mr-2 h-4 w-4" />
              Try again
            </Button>
          </div>
        </GameShell>
      );
    }

    if (isSessionComplete) {
      return (
        <GameShell>
          <div className="space-y-6 py-6 text-center">
            <CheckCircle2
              className="mx-auto h-14 w-14 text-green-500"
              aria-hidden="true"
            />

            <div className="space-y-2">
              <h2 className="text-2xl font-bold">
                {completedRatings.length > 0
                  ? "Review session completed!"
                  : "You're all caught up!"}
              </h2>

              <p className="text-muted-foreground">
                {completedRatings.length > 0
                  ? `You reviewed ${completedRatings.length} flashcards.`
                  : "No flashcards are available for review right now."}
              </p>
            </div>

            {completedRatings.length > 0 && (
              <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
                {reviewRatingOptions.map((ratingOption) => {
                  const ratingCount = completedRatings.filter(
                    (completedRating) =>
                      completedRating === ratingOption.rating,
                  ).length;

                  return (
                    <div
                      key={ratingOption.rating}
                      className="rounded-xl border border-border bg-muted/30 p-4"
                    >
                      <p className="text-2xl font-bold">{ratingCount}</p>

                      <p className="text-sm text-muted-foreground">
                        {ratingOption.label}
                      </p>
                    </div>
                  );
                })}
              </div>
            )}

            <Button type="button" variant="outline" onClick={reloadReviewQueue}>
              <RefreshCw className="mr-2 h-4 w-4" />
              Check for more reviews
            </Button>
          </div>
        </GameShell>
      );
    }

    if (!currentReviewCard) {
      return null;
    }

    return (
      <div className="space-y-5">
        <GameProgress
          current={currentCardIndex}
          total={reviewQueue.length}
          label="Review progress"
        />

        <div className="flex items-center justify-between text-sm text-muted-foreground">
          <span>
            Card {currentCardIndex + 1} of {reviewQueue.length}
          </span>

          <span className="rounded-full border border-border bg-card px-3 py-1 text-xs font-medium">
            {formatReviewStage(currentReviewCard.stage)}
          </span>
        </div>

        <StudyFlashcardCard
          currentFlashcard={displayedFlashcard}
          isFlipped={isFlipped}
          onFlip={handleFlashcardFlip}
          onSpeak={speakFlashcardText}
        />

        <GameShell>
          {!isFlipped ? (
            <Button
              type="button"
              size="lg"
              className="w-full bg-pink-500 text-white hover:bg-pink-600"
              disabled={
                isSubmitting ||
                pendingReviewRequest !== null ||
                requiresQueueRefresh
              }
              onClick={() => setIsFlipped(true)}
            >
              <Eye className="mr-2 h-4 w-4" />
              Show answer
            </Button>
          ) : (
            <div className="space-y-4">
              <p className="text-center text-sm text-muted-foreground">
                How well did you remember?
              </p>

              <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
                {reviewRatingOptions.map((ratingOption) => {
                  const RatingIcon = ratingOption.icon;

                  return (
                    <Button
                      key={ratingOption.rating}
                      type="button"
                      variant="outline"
                      disabled={
                        isSubmitting ||
                        requiresQueueRefresh ||
                        pendingReviewRequest !== null
                      }
                      className={`h-auto min-h-20 flex-col gap-1 py-3 ${ratingOption.className}`}
                      onClick={() => handleRatingSelected(ratingOption.rating)}
                    >
                      <RatingIcon className="h-5 w-5" aria-hidden="true" />

                      <span className="font-semibold">
                        {ratingOption.label}
                      </span>

                      <span className="text-xs font-normal">
                        {ratingOption.description}
                      </span>
                    </Button>
                  );
                })}
              </div>
            </div>
          )}

          {isSubmitting && (
            <div
              role="status"
              className="flex items-center justify-center gap-2 text-sm text-muted-foreground"
            >
              <Loader2 className="h-4 w-4 animate-spin" />
              Saving review...
            </div>
          )}

          {submissionError && (
            <div
              role="alert"
              className="space-y-3 rounded-xl border border-destructive/30 bg-destructive/10 p-4"
            >
              <p className="text-sm text-destructive">{submissionError}</p>

              <div className="flex flex-wrap gap-2">
                {pendingReviewRequest ? (
                  <Button
                    type="button"
                    size="sm"
                    disabled={isSubmitting}
                    onClick={retryPendingReview}
                  >
                    <RefreshCw className="mr-2 h-4 w-4" />
                    Retry saved rating
                  </Button>
                ) : (
                  <Button
                    type="button"
                    size="sm"
                    variant="outline"
                    disabled={isSubmitting}
                    onClick={reloadReviewQueue}
                  >
                    <RefreshCw className="mr-2 h-4 w-4" />
                    Reload session
                  </Button>
                )}
              </div>

              {pendingReviewRequest && (
                <p className="text-xs text-muted-foreground">
                  Your rating may have been saved. Retrying will reuse the same
                  request without recording it twice.
                </p>
              )}
            </div>
          )}
        </GameShell>

        <p className="text-center text-xs text-muted-foreground">
          Each review is saved immediately. You can leave at any time after
          saving.
        </p>
      </div>
    );
  })();

  return (
    <GamePageLayout
      nickname={ownerNickname}
      setId={flashcardSetId}
      warnBeforeLeaving={isSubmitting || pendingReviewRequest !== null}
    >
      <div className="mx-auto max-w-2xl space-y-6">
        <header className="space-y-2">
          <div className="flex items-center gap-2 text-pink-500 dark:text-pink-400">
            <BrainCircuit className="h-5 w-5" aria-hidden="true" />

            <span className="text-sm font-semibold">Spaced repetition</span>
          </div>

          <h1 className="text-2xl font-bold text-foreground md:text-3xl">
            Flashcard reviews
          </h1>

          <p className="text-sm text-muted-foreground">
            Recall the answer, flip the card, and rate your memory.
          </p>
        </header>

        {reviewContent}
      </div>
    </GamePageLayout>
  );
}

/**
 * Displays a loading state using the existing game layout.
 */
function ReviewStatusMessage({ message }: { message: string }) {
  return (
    <GameShell>
      <div
        role="status"
        className="flex items-center justify-center gap-3 py-12 text-muted-foreground"
      >
        <Loader2
          className="h-5 w-5 animate-spin text-pink-500"
          aria-hidden="true"
        />

        <span>{message}</span>
      </div>
    </GameShell>
  );
}

/**
 * Converts an FSRS learning stage into a readable label.
 */
function formatReviewStage(stage: ReviewCard["stage"]): string {
  switch (stage) {
    case "NEW":
      return "New";

    case "LEARNING":
      return "Learning";

    case "REVIEW":
      return "Review";

    case "RELEARNING":
      return "Relearning";
  }
}

/**
 * Converts API failures into a user-friendly message.
 */
function getReviewErrorMessage(
  error: unknown,
  fallbackMessage: string,
): string {
  if (error instanceof ApiError) {
    if (error.status === 401) {
      return "Your session has expired. Please sign in again.";
    }

    if (error.status === 403) {
      return "You don't have permission to review this set.";
    }

    if (error.status === 409) {
      return "This flashcard is no longer due. Reload your session.";
    }

    return error.message;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return fallbackMessage;
}
