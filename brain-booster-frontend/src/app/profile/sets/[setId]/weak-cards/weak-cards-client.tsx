"use client";

import { useMemo, useState, type ComponentProps, type ReactNode } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import {
  ArrowLeft,
  Brain,
  CheckCircle2,
  RefreshCw,
  RotateCcw,
} from "lucide-react";

import { useAuth } from "@/context/AuthContext";
import { useWeakFlashcards } from "@/hooks/game-results";
import type { WeakFlashcard } from "@/types/games";

import StudyFlashcardCard from "@/app/users/[nickname]/sets/components/study-flashcard-card";
import StudyControls from "@/app/users/[nickname]/sets/components/study-controls";
import StudyProgress from "@/app/users/[nickname]/sets/components/study-progress";

type StudyFlashcard = NonNullable<
  ComponentProps<typeof StudyFlashcardCard>["currentFlashcard"]
>;

function formatPercentage(value: number | null | undefined) {
  return `${Math.round(value ?? 0)}%`;
}

function clampPercentage(value: number | null | undefined) {
  return Math.min(Math.max(value ?? 0, 0), 100);
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
  type?: "default" | "error";
  children: ReactNode;
}) {
  return (
    <p
      className={
        type === "error"
          ? "rounded-xl border border-pink-200 bg-pink-50 px-4 py-3 text-sm text-pink-600"
          : "rounded-xl border border-dashed border-gray-200 bg-white px-4 py-3 text-sm text-slate-500"
      }
    >
      {children}
    </p>
  );
}

function WeakCardStats({ flashcard }: { flashcard: WeakFlashcard }) {
  return (
    <section className="grid gap-3 sm:grid-cols-3">
      <div className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
        <p className="text-sm font-medium text-slate-500">Accuracy</p>

        <p className="mt-2 text-xl font-bold text-slate-950">
          {formatPercentage(flashcard.accuracyPercentage)}
        </p>

        <div className="mt-3 h-2 overflow-hidden rounded-full bg-gray-100">
          <div
            className="h-full rounded-full bg-pink-500 transition-all"
            style={{
              width: `${clampPercentage(flashcard.accuracyPercentage)}%`,
            }}
          />
        </div>
      </div>

      <div className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
        <p className="text-sm font-medium text-green-600">Correct answers</p>

        <p className="mt-2 text-xl font-bold text-slate-950">
          {flashcard.correctAnswers}
        </p>
      </div>

      <div className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
        <p className="text-sm font-medium text-red-600">Incorrect answers</p>

        <p className="mt-2 text-xl font-bold text-slate-950">
          {flashcard.incorrectAnswers}
        </p>
      </div>
    </section>
  );
}

export default function WeakCardsClient() {
  const params = useParams<{ setId: string }>();
  const { token, isAuthenticated, isAuthLoading } = useAuth();

  const parsedSetId = Number(params.setId);

  const setId =
    Number.isFinite(parsedSetId) && parsedSetId > 0 ? parsedSetId : null;

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

  const studyFlashcards = useMemo(() => {
    return orderedWeakFlashcards.map(
      (flashcard) =>
        ({
          flashcardId: flashcard.flashcardId,
          term: flashcard.term,
          definition: flashcard.definition,
          starred: false,
        }) as StudyFlashcard,
    );
  }, [orderedWeakFlashcards]);

  const safeCurrentIndex =
    studyFlashcards.length > 0
      ? Math.min(currentIndex, studyFlashcards.length - 1)
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

    setCurrentIndex((previousIndex) => previousIndex - 1);
    setIsFlipped(false);
  }

  function handleNextCard() {
    if (isLastCard) {
      return;
    }

    setCurrentIndex((previousIndex) => previousIndex + 1);
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
    if (!("speechSynthesis" in window)) {
      return;
    }

    speechSynthesis.cancel();

    const utterance = new SpeechSynthesisUtterance(text);
    speechSynthesis.speak(utterance);
  }

  if (isAuthLoading) {
    return (
      <main className="min-h-[calc(100vh-4rem)] bg-gray-50">
        <div className="mx-auto max-w-5xl px-4 py-10">
          <StatusMessage>Loading weak cards...</StatusMessage>
        </div>
      </main>
    );
  }

  if (!token || !isAuthenticated) {
    return (
      <main className="min-h-[calc(100vh-4rem)] bg-gray-50">
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
      <main className="min-h-[calc(100vh-4rem)] bg-gray-50">
        <div className="mx-auto max-w-5xl px-4 py-10">
          <StatusMessage type="error">Invalid flashcard set ID.</StatusMessage>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-[calc(100vh-4rem)] bg-gray-50">
      <div className="mx-auto max-w-5xl px-4 pt-8 pb-28">
        <Link
          href={`/profile/sets/${setId}/stats`}
          className="inline-flex items-center gap-2 text-sm font-medium text-slate-500 transition hover:text-pink-500"
        >
          <ArrowLeft size={16} />
          Back to stats
        </Link>

        <section className="mt-6 overflow-hidden rounded-3xl border border-gray-200 bg-white shadow-sm">
          <div className="bg-pink-50/70 p-6 sm:p-8">
            <p className="mb-3 inline-flex items-center gap-2 rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500">
              <Brain size={14} />
              Weak cards review
            </p>

            <div className="flex flex-col gap-5 md:flex-row md:items-end md:justify-between">
              <div>
                <h1 className="text-3xl font-bold tracking-tight text-slate-950 sm:text-4xl">
                  Review weak cards
                </h1>

                <p className="mt-3 max-w-2xl text-slate-500">
                  Focus only on flashcards where you previously made mistakes.
                </p>
              </div>

              {studyFlashcards.length > 0 ? (
                <button
                  type="button"
                  onClick={handleRestart}
                  className="inline-flex shrink-0 items-center justify-center gap-2 rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm font-semibold text-slate-600 transition hover:border-pink-200 hover:text-pink-500"
                >
                  <RotateCcw size={16} />
                  Restart review
                </button>
              ) : null}
            </div>
          </div>
        </section>

        {isLoading ? (
          <div className="mt-6">
            <StatusMessage>Loading weak flashcards...</StatusMessage>
          </div>
        ) : null}

        {error ? (
          <div className="mt-6">
            <StatusMessage type="error">{error}</StatusMessage>
          </div>
        ) : null}

        {!isLoading && !error && studyFlashcards.length === 0 ? (
          <section className="mt-6 rounded-3xl border border-dashed border-gray-200 bg-white p-10 text-center shadow-sm">
            <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-green-50 text-green-600">
              <CheckCircle2 size={28} />
            </div>

            <h2 className="mt-4 text-xl font-semibold text-slate-950">
              No weak cards
            </h2>

            <p className="mx-auto mt-2 max-w-md text-sm text-slate-500">
              There are currently no weak flashcards in this set.
            </p>

            <button
              type="button"
              onClick={() => void refetch()}
              className="mt-5 inline-flex items-center gap-2 rounded-xl bg-pink-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-pink-600"
            >
              <RefreshCw size={16} />
              Refresh
            </button>
          </section>
        ) : null}

        {currentFlashcard && currentWeakFlashcard ? (
          <div className="mx-auto mt-6 max-w-2xl">
            <section className="mb-4 rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
              <div className="flex items-center justify-between gap-4 text-sm">
                <span className="font-medium text-slate-500">
                  Card {safeCurrentIndex + 1} of {studyFlashcards.length}
                </span>

                <span className="font-semibold text-pink-500">
                  {formatPercentage(sessionProgress)}
                </span>
              </div>

              <div className="mt-3 h-2 overflow-hidden rounded-full bg-gray-100">
                <div
                  className="h-full rounded-full bg-pink-500 transition-all"
                  style={{
                    width: `${clampPercentage(sessionProgress)}%`,
                  }}
                />
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

            {isProgressTrackingEnabled ? (
              <StudyProgress
                knownCount={knownFlashcards.size}
                unknownCount={unknownFlashcards.size}
                totalCount={studyFlashcards.length}
              />
            ) : null}

            <div className="mt-6">
              <WeakCardStats flashcard={currentWeakFlashcard} />
            </div>

            {isLastCard ? (
              <section className="mt-6 rounded-3xl border border-green-200 bg-green-50 p-6 text-center">
                <CheckCircle2 size={32} className="mx-auto text-green-600" />

                <h2 className="mt-3 text-xl font-semibold text-slate-950">
                  Review completed
                </h2>

                <p className="mt-2 text-sm text-slate-500">
                  You reviewed {reviewedFlashcardsCount} of{" "}
                  {studyFlashcards.length} weak cards.
                </p>

                <div className="mt-5 flex flex-col justify-center gap-3 sm:flex-row">
                  <button
                    type="button"
                    onClick={handleRestart}
                    className="inline-flex items-center justify-center gap-2 rounded-xl border border-green-200 bg-white px-5 py-3 text-sm font-semibold text-green-700 transition hover:bg-green-100"
                  >
                    <RotateCcw size={17} />
                    Review again
                  </button>

                  <Link
                    href={`/profile/sets/${setId}/stats`}
                    className="inline-flex items-center justify-center gap-2 rounded-xl bg-green-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-green-700"
                  >
                    <CheckCircle2 size={17} />
                    Finish review
                  </Link>
                </div>
              </section>
            ) : null}
          </div>
        ) : null}
      </div>
    </main>
  );
}
