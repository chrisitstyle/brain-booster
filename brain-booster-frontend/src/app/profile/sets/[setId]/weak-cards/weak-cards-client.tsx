"use client";

import { useMemo, useState, type ComponentProps, type ReactNode } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import {
  AlertCircle,
  ArrowLeft,
  Brain,
  CheckCircle2,
  Loader2,
  RefreshCw,
  RotateCcw,
} from "lucide-react";

import StudyControls from "@/app/users/[nickname]/sets/components/study-controls";
import StudyFlashcardCard from "@/app/users/[nickname]/sets/components/study-flashcard-card";
import StudyProgress from "@/app/users/[nickname]/sets/components/study-progress";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/context/AuthContext";
import { useWeakFlashcards } from "@/hooks/game-results";
import type { WeakFlashcard } from "@/types/games";

type StudyFlashcard = NonNullable<
  ComponentProps<typeof StudyFlashcardCard>["currentFlashcard"]
>;

function clampPercentage(value: number | null | undefined) {
  const normalizedValue =
    typeof value === "number" && Number.isFinite(value) ? value : 0;

  return Math.min(Math.max(normalizedValue, 0), 100);
}

function formatPercentage(value: number | null | undefined) {
  return `${Math.round(clampPercentage(value))}%`;
}

function createSeededRandom(seed: number) {
  let currentValue = seed || 1;

  return () => {
    currentValue = (currentValue * 1_664_525 + 1_013_904_223) % 4_294_967_296;

    return currentValue / 4_294_967_296;
  };
}

function shuffleCards(cards: WeakFlashcard[], seed: number) {
  const shuffledCards = [...cards];
  const random = createSeededRandom(seed);

  for (let index = shuffledCards.length - 1; index > 0; index -= 1) {
    const randomIndex = Math.floor(random() * (index + 1));

    [shuffledCards[index], shuffledCards[randomIndex]] = [
      shuffledCards[randomIndex],
      shuffledCards[index],
    ];
  }

  return shuffledCards;
}

function StatusMessage({
  type = "default",
  children,
}: {
  type?: "default" | "error" | "loading";
  children: ReactNode;
}) {
  if (type === "error") {
    return (
      <div
        className="flex items-start gap-3 rounded-xl border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive"
        role="alert"
      >
        <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />

        <span>{children}</span>
      </div>
    );
  }

  return (
    <div
      className="flex items-center gap-3 rounded-xl border border-dashed border-border bg-card px-4 py-3 text-sm text-muted-foreground"
      role={type === "loading" ? "status" : undefined}
    >
      {type === "loading" && (
        <Loader2
          className="h-4 w-4 shrink-0 animate-spin text-pink-500 dark:text-pink-400"
          aria-hidden="true"
        />
      )}

      <span>{children}</span>
    </div>
  );
}

function ProgressBar({ value }: { value: number | null | undefined }) {
  const clampedValue = clampPercentage(value);

  return (
    <div
      className="h-2 w-full overflow-hidden rounded-full bg-muted"
      role="progressbar"
      aria-valuemin={0}
      aria-valuemax={100}
      aria-valuenow={Math.round(clampedValue)}
    >
      <div
        className="h-full rounded-full bg-pink-500 transition-all"
        style={{
          width: `${clampedValue}%`,
        }}
      />
    </div>
  );
}

function WeakCardStat({
  label,
  value,
  variant = "default",
  children,
}: {
  label: string;
  value: ReactNode;
  variant?: "default" | "success" | "error";
  children?: ReactNode;
}) {
  const labelClassName =
    variant === "success"
      ? "text-green-600 dark:text-green-400"
      : variant === "error"
        ? "text-red-600 dark:text-red-400"
        : "text-muted-foreground";

  return (
    <div className="rounded-2xl border border-border bg-card p-4 text-card-foreground shadow-sm">
      <p className={`text-sm font-medium ${labelClassName}`}>{label}</p>

      <p className="mt-2 text-xl font-bold text-card-foreground">{value}</p>

      {children}
    </div>
  );
}

function WeakCardStats({ flashcard }: { flashcard: WeakFlashcard }) {
  return (
    <section
      className="grid gap-3 sm:grid-cols-3"
      aria-label="Weak flashcard statistics"
    >
      <WeakCardStat
        label="Accuracy"
        value={formatPercentage(flashcard.accuracyPercentage)}
      >
        <div className="mt-3">
          <ProgressBar value={flashcard.accuracyPercentage} />
        </div>
      </WeakCardStat>

      <WeakCardStat
        label="Correct answers"
        value={flashcard.correctAnswers}
        variant="success"
      />

      <WeakCardStat
        label="Incorrect answers"
        value={flashcard.incorrectAnswers}
        variant="error"
      />
    </section>
  );
}

export default function WeakCardsClient() {
  const params = useParams<{
    setId: string;
  }>();

  const { token, isAuthenticated, isAuthLoading } = useAuth();

  const parsedSetId = Number(params.setId);

  const setId =
    Number.isInteger(parsedSetId) && parsedSetId > 0 ? parsedSetId : null;

  const {
    data: weakFlashcards,
    isLoading,
    error,
    refetch,
  } = useWeakFlashcards(setId, token);

  const [currentIndex, setCurrentIndex] = useState(0);

  const [isFlipped, setIsFlipped] = useState(false);

  const [shuffleSeed, setShuffleSeed] = useState(0);

  const [isProgressTrackingEnabled, setIsProgressTrackingEnabled] =
    useState(true);

  const [knownFlashcards, setKnownFlashcards] = useState<Set<number>>(
    new Set(),
  );

  const [unknownFlashcards, setUnknownFlashcards] = useState<Set<number>>(
    new Set(),
  );

  const orderedWeakFlashcards = useMemo(() => {
    const cards = weakFlashcards ?? [];

    if (shuffleSeed === 0) {
      return cards;
    }

    return shuffleCards(cards, shuffleSeed);
  }, [weakFlashcards, shuffleSeed]);

  const studyFlashcards = useMemo(
    () =>
      orderedWeakFlashcards.map(
        (flashcard) =>
          ({
            flashcardId: flashcard.flashcardId,
            term: flashcard.term,
            definition: flashcard.definition,
            starred: false,
          }) as StudyFlashcard,
      ),
    [orderedWeakFlashcards],
  );

  const safeCurrentIndex =
    studyFlashcards.length > 0
      ? Math.min(Math.max(currentIndex, 0), studyFlashcards.length - 1)
      : 0;

  const currentFlashcard = studyFlashcards[safeCurrentIndex];

  const currentWeakFlashcard = orderedWeakFlashcards[safeCurrentIndex];

  const isFirstCard = safeCurrentIndex === 0;

  const isLastCard =
    studyFlashcards.length > 0 &&
    safeCurrentIndex === studyFlashcards.length - 1;

  const reviewedFlashcardsCount = new Set([
    ...knownFlashcards,
    ...unknownFlashcards,
  ]).size;

  const sessionProgress =
    studyFlashcards.length > 0
      ? ((safeCurrentIndex + 1) / studyFlashcards.length) * 100
      : 0;

  function handleFlipCard() {
    setIsFlipped((previousValue) => !previousValue);
  }

  function handlePreviousCard() {
    if (isFirstCard) {
      return;
    }

    setCurrentIndex(Math.max(safeCurrentIndex - 1, 0));

    setIsFlipped(false);
  }

  function handleNextCard() {
    if (isLastCard) {
      return;
    }

    setCurrentIndex(Math.min(safeCurrentIndex + 1, studyFlashcards.length - 1));

    setIsFlipped(false);
  }

  function handleKnownCard() {
    if (!currentFlashcard) {
      return;
    }

    if (isProgressTrackingEnabled) {
      setKnownFlashcards((previousKnownFlashcards) => {
        const nextKnownFlashcards = new Set(previousKnownFlashcards);

        nextKnownFlashcards.add(currentFlashcard.flashcardId);

        return nextKnownFlashcards;
      });

      setUnknownFlashcards((previousUnknownFlashcards) => {
        const nextUnknownFlashcards = new Set(previousUnknownFlashcards);

        nextUnknownFlashcards.delete(currentFlashcard.flashcardId);

        return nextUnknownFlashcards;
      });
    }

    handleNextCard();
  }

  function handleUnknownCard() {
    if (!currentFlashcard) {
      return;
    }

    if (isProgressTrackingEnabled) {
      setUnknownFlashcards((previousUnknownFlashcards) => {
        const nextUnknownFlashcards = new Set(previousUnknownFlashcards);

        nextUnknownFlashcards.add(currentFlashcard.flashcardId);

        return nextUnknownFlashcards;
      });

      setKnownFlashcards((previousKnownFlashcards) => {
        const nextKnownFlashcards = new Set(previousKnownFlashcards);

        nextKnownFlashcards.delete(currentFlashcard.flashcardId);

        return nextKnownFlashcards;
      });
    }

    handleNextCard();
  }

  function handleShuffleCards() {
    setShuffleSeed((previousSeed) => previousSeed + 1);

    setCurrentIndex(0);
    setIsFlipped(false);
  }

  function handleRestart() {
    setShuffleSeed(0);
    setCurrentIndex(0);
    setIsFlipped(false);
    setKnownFlashcards(new Set());
    setUnknownFlashcards(new Set());
  }

  function speakText(text: string) {
    if (typeof window === "undefined" || !("speechSynthesis" in window)) {
      return;
    }

    const trimmedText = text.trim();

    if (!trimmedText) {
      return;
    }

    window.speechSynthesis.cancel();

    const utterance = new SpeechSynthesisUtterance(trimmedText);

    window.speechSynthesis.speak(utterance);
  }

  if (isAuthLoading) {
    return (
      <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
        <div className="mx-auto max-w-5xl px-4 py-10">
          <StatusMessage type="loading">Loading weak cards...</StatusMessage>
        </div>
      </main>
    );
  }

  if (!token || !isAuthenticated) {
    return (
      <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
        <div className="mx-auto max-w-5xl px-4 py-10">
          <StatusMessage type="error">
            You need to be logged in to review weak cards.
          </StatusMessage>
        </div>
      </main>
    );
  }

  if (!setId) {
    return (
      <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
        <div className="mx-auto max-w-5xl px-4 py-10">
          <StatusMessage type="error">Invalid flashcard set ID.</StatusMessage>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
      <div className="mx-auto max-w-5xl px-4 pb-28 pt-8">
        <Link
          href={`/profile/sets/${setId}/stats`}
          className="inline-flex items-center gap-2 rounded-sm text-sm font-medium text-muted-foreground transition-colors hover:text-pink-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring dark:hover:text-pink-400"
        >
          <ArrowLeft size={16} aria-hidden="true" />
          Back to stats
        </Link>

        <section className="mt-6 overflow-hidden rounded-3xl border border-border bg-card text-card-foreground shadow-sm">
          <div className="bg-pink-50/70 p-6 dark:bg-pink-950/20 sm:p-8">
            <p className="mb-3 inline-flex items-center gap-2 rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
              <Brain size={14} aria-hidden="true" />
              Weak cards review
            </p>

            <div className="flex flex-col gap-5 md:flex-row md:items-end md:justify-between">
              <div>
                <h1 className="text-3xl font-bold tracking-tight text-card-foreground sm:text-4xl">
                  Review weak cards
                </h1>

                <p className="mt-3 max-w-2xl text-muted-foreground">
                  Focus only on flashcards where you previously made mistakes.
                </p>
              </div>

              {studyFlashcards.length > 0 && (
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleRestart}
                  className="shrink-0 border-border bg-background text-muted-foreground hover:border-pink-200 hover:bg-pink-50 hover:text-pink-500 dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-400"
                >
                  <RotateCcw className="mr-2 h-4 w-4" aria-hidden="true" />
                  Restart review
                </Button>
              )}
            </div>
          </div>
        </section>

        {isLoading && (
          <div className="mt-6">
            <StatusMessage type="loading">
              Loading weak flashcards...
            </StatusMessage>
          </div>
        )}

        {error && (
          <div className="mt-6">
            <StatusMessage type="error">{error}</StatusMessage>
          </div>
        )}

        {!isLoading && !error && studyFlashcards.length === 0 && (
          <section className="mt-6 rounded-3xl border border-dashed border-border bg-card p-10 text-center text-card-foreground shadow-sm">
            <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-green-100 text-green-600 dark:bg-green-950/50 dark:text-green-400">
              <CheckCircle2 size={28} aria-hidden="true" />
            </div>

            <h2 className="mt-4 text-xl font-semibold text-card-foreground">
              No weak cards
            </h2>

            <p className="mx-auto mt-2 max-w-md text-sm text-muted-foreground">
              There are currently no weak flashcards in this set.
            </p>

            <Button
              type="button"
              onClick={() => {
                void refetch();
              }}
              className="mt-5 bg-pink-500 text-white hover:bg-pink-600"
            >
              <RefreshCw className="mr-2 h-4 w-4" aria-hidden="true" />
              Refresh
            </Button>
          </section>
        )}

        {!isLoading && !error && currentFlashcard && currentWeakFlashcard && (
          <div className="mx-auto mt-6 max-w-2xl">
            <section
              className="mb-4 rounded-2xl border border-border bg-card p-4 text-card-foreground shadow-sm"
              aria-label="Review progress"
            >
              <div className="flex items-center justify-between gap-4 text-sm">
                <span className="font-medium text-muted-foreground">
                  Card {safeCurrentIndex + 1} of {studyFlashcards.length}
                </span>

                <span className="font-semibold text-pink-500 dark:text-pink-400">
                  {formatPercentage(sessionProgress)}
                </span>
              </div>

              <div className="mt-3">
                <ProgressBar value={sessionProgress} />
              </div>
            </section>

            <StudyFlashcardCard
              currentFlashcard={currentFlashcard}
              isFlipped={isFlipped}
              onFlip={handleFlipCard}
              onSpeak={speakText}
            />

            <StudyControls
              currentIndex={safeCurrentIndex}
              totalCards={studyFlashcards.length}
              isProgressTrackingEnabled={isProgressTrackingEnabled}
              onToggleProgressTracking={() =>
                setIsProgressTrackingEnabled((previousValue) => !previousValue)
              }
              onUnknown={handleUnknownCard}
              onPrevious={handlePreviousCard}
              onNext={handleNextCard}
              onKnown={handleKnownCard}
              onShuffle={handleShuffleCards}
            />

            {isProgressTrackingEnabled && (
              <StudyProgress
                knownCount={knownFlashcards.size}
                unknownCount={unknownFlashcards.size}
                totalCount={studyFlashcards.length}
              />
            )}

            <div className="mt-6">
              <WeakCardStats flashcard={currentWeakFlashcard} />
            </div>

            {isLastCard && (
              <section className="mt-6 rounded-3xl border border-green-200 bg-green-50 p-6 text-center dark:border-green-900 dark:bg-green-950/20">
                <CheckCircle2
                  size={32}
                  className="mx-auto text-green-600 dark:text-green-400"
                  aria-hidden="true"
                />

                <h2 className="mt-3 text-xl font-semibold text-foreground">
                  Review completed
                </h2>

                <p className="mt-2 text-sm text-muted-foreground">
                  {isProgressTrackingEnabled
                    ? `You reviewed ${reviewedFlashcardsCount} of ${studyFlashcards.length} weak cards.`
                    : `You reached the end of all ${studyFlashcards.length} weak cards.`}
                </p>

                <div className="mt-5 flex flex-col justify-center gap-3 sm:flex-row">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={handleRestart}
                    className="border-green-200 bg-background text-green-700 hover:bg-green-100 dark:border-green-900 dark:text-green-400 dark:hover:bg-green-950/50"
                  >
                    <RotateCcw className="mr-2 h-4 w-4" aria-hidden="true" />
                    Review again
                  </Button>

                  <Button
                    asChild
                    className="bg-green-600 text-white hover:bg-green-700"
                  >
                    <Link href={`/profile/sets/${setId}/stats`}>
                      <CheckCircle2
                        className="mr-2 h-4 w-4"
                        aria-hidden="true"
                      />
                      Finish review
                    </Link>
                  </Button>
                </div>
              </section>
            )}
          </div>
        )}
      </div>
    </main>
  );
}
