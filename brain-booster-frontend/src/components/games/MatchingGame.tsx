"use client";

import { useMemo } from "react";

import type { Flashcard } from "@/api/flashcardService";
import {
  getElapsedGameSeconds,
  useGameElapsedSeconds,
} from "@/components/games/hooks/useGameElapsedSeconds";
import { useSaveGameResultOnFinish } from "@/components/games/hooks/useSaveGameResultOnFinish";
import { getGameStorageKey } from "@/components/games/shared/game-storage";
import GameEmptyState from "@/components/games/shared/GameEmptyState";
import GameProgress from "@/components/games/shared/GameProgress";
import GameResultCard from "@/components/games/shared/GameResultCard";
import GameShell from "@/components/games/shared/GameShell";
import GameTimer from "@/components/games/shared/GameTimer";
import { usePersistedGameState } from "@/components/games/shared/usePersistedGameState";
import { createMatchingQuestionResults } from "@/components/games/utils/gameQuestionResults";
import { Button } from "@/components/ui/button";

import { shuffleArray } from "./game-utils";

interface MatchingGameProps {
  flashcards: Flashcard[];
  setId: number | string;
}

interface MismatchPair {
  termId: string;
  definitionId: string;
}

interface MatchingGameState {
  roundId: number;
  cardsForRound: Flashcard[];
  definitionCards: Flashcard[];
  selectedTermId: string | null;
  selectedDefinitionId: string | null;
  matchedIds: string[];
  mistakes: number;
  mistakesByFlashcardId: Record<number, number>;
  mismatchPair: MismatchPair | null;
  isResultSaved: boolean;
  startedAt: number;
  finishedAt: number | null;
}

function getFlashcardId(card: Flashcard) {
  return String(card.flashcardId);
}

function getCurrentTimestamp() {
  return Date.now();
}

function incrementMistakesByFlashcardId({
  mistakesByFlashcardId,
  termId,
  definitionId,
}: {
  mistakesByFlashcardId: Record<number, number>;
  termId: string;
  definitionId: string;
}) {
  const termFlashcardId = Number(termId);
  const definitionFlashcardId = Number(definitionId);

  return {
    ...mistakesByFlashcardId,
    [termFlashcardId]: (mistakesByFlashcardId[termFlashcardId] ?? 0) + 1,
    [definitionFlashcardId]:
      (mistakesByFlashcardId[definitionFlashcardId] ?? 0) + 1,
  };
}

function createNewMatchingRound(
  flashcards: Flashcard[],
  roundId = 0,
): MatchingGameState {
  const cardsForRound = shuffleArray(flashcards).slice(
    0,
    Math.min(6, flashcards.length),
  );

  return {
    roundId,
    cardsForRound,
    definitionCards: shuffleArray(cardsForRound),
    selectedTermId: null,
    selectedDefinitionId: null,
    matchedIds: [],
    mistakes: 0,
    mistakesByFlashcardId: {},
    mismatchPair: null,
    isResultSaved: false,
    startedAt: Date.now(),
    finishedAt: null,
  };
}

const baseButtonClass =
  "h-auto min-h-14 w-full min-w-0 justify-start whitespace-normal break-words rounded-xl border-border bg-background px-4 py-3 text-left leading-relaxed text-foreground transition-colors hover:border-pink-300 hover:bg-pink-50 hover:text-pink-600 disabled:opacity-100 dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-400";

const selectedButtonClass =
  "h-auto min-h-14 w-full min-w-0 justify-start whitespace-normal break-words rounded-xl border-pink-400 bg-pink-50 px-4 py-3 text-left leading-relaxed text-pink-600 ring-2 ring-pink-100 transition disabled:opacity-100 dark:border-pink-700 dark:bg-pink-950/40 dark:text-pink-400 dark:ring-pink-900/50";

const mismatchButtonClass =
  "game-shake h-auto min-h-14 w-full min-w-0 justify-start whitespace-normal break-words rounded-xl border-red-300 bg-red-50 px-4 py-3 text-left leading-relaxed text-red-700 transition disabled:opacity-100 dark:border-red-900 dark:bg-red-950/30 dark:text-red-400";

const matchedButtonClass =
  "h-auto min-h-14 w-full min-w-0 justify-start whitespace-normal break-words rounded-xl border-green-200 bg-green-50 px-4 py-3 text-left leading-relaxed text-green-700 transition disabled:opacity-100 dark:border-green-900 dark:bg-green-950/30 dark:text-green-400";

export default function MatchingGame({ flashcards, setId }: MatchingGameProps) {
  const storageKey = getGameStorageKey(setId, "matching");

  const [gameState, setGameState, clearGameState] =
    usePersistedGameState<MatchingGameState>(storageKey, () =>
      createNewMatchingRound(flashcards),
    );

  const {
    roundId,
    cardsForRound,
    definitionCards,
    selectedTermId,
    selectedDefinitionId,
    matchedIds,
    mistakes,
    mistakesByFlashcardId = {},
    mismatchPair,
    isResultSaved,
    startedAt,
    finishedAt,
  } = gameState;

  const isFinished =
    cardsForRound.length > 0 && matchedIds.length === cardsForRound.length;

  const finalScore = Math.max(cardsForRound.length - mistakes, 0);

  const questionResults = useMemo(
    () =>
      isFinished
        ? createMatchingQuestionResults({
            cards: cardsForRound,
            mistakesByFlashcardId,
          })
        : [],
    [cardsForRound, mistakesByFlashcardId, isFinished],
  );

  const elapsedSeconds = useGameElapsedSeconds(startedAt, finishedAt);

  const durationSeconds = isFinished
    ? getElapsedGameSeconds(startedAt, finishedAt)
    : undefined;

  function updateGameState(nextState: Partial<MatchingGameState>) {
    setGameState((previousState) => ({
      ...previousState,
      ...nextState,
    }));
  }

  useSaveGameResultOnFinish({
    setId,
    mode: "matching",
    score: finalScore,
    totalQuestions: cardsForRound.length,
    durationSeconds,
    questionResults,
    isFinished,
    isResultSaved,
    onSaved: () => {
      updateGameState({
        isResultSaved: true,
      });
    },
  });

  function isMatched(cardId: string) {
    return matchedIds.includes(cardId);
  }

  function resetGame() {
    clearGameState();

    setGameState(createNewMatchingRound(flashcards, roundId + 1));
  }

  function checkPair(termId: string, definitionId: string) {
    if (termId === definitionId) {
      const nextMatchedIds = matchedIds.includes(termId)
        ? matchedIds
        : [...matchedIds, termId];

      updateGameState({
        matchedIds: nextMatchedIds,
        selectedTermId: null,
        selectedDefinitionId: null,
        finishedAt:
          nextMatchedIds.length === cardsForRound.length
            ? getCurrentTimestamp()
            : finishedAt,
      });

      return;
    }

    updateGameState({
      mistakes: mistakes + 1,
      mistakesByFlashcardId: incrementMistakesByFlashcardId({
        mistakesByFlashcardId,
        termId,
        definitionId,
      }),
      mismatchPair: {
        termId,
        definitionId,
      },
    });

    window.setTimeout(() => {
      updateGameState({
        selectedTermId: null,
        selectedDefinitionId: null,
        mismatchPair: null,
      });
    }, 600);
  }

  function handleTermClick(card: Flashcard) {
    const cardId = getFlashcardId(card);

    if (isMatched(cardId) || mismatchPair) {
      return;
    }

    updateGameState({
      selectedTermId: cardId,
    });

    if (selectedDefinitionId) {
      checkPair(cardId, selectedDefinitionId);
    }
  }

  function handleDefinitionClick(card: Flashcard) {
    const cardId = getFlashcardId(card);

    if (isMatched(cardId) || mismatchPair) {
      return;
    }

    updateGameState({
      selectedDefinitionId: cardId,
    });

    if (selectedTermId) {
      checkPair(selectedTermId, cardId);
    }
  }

  function getTermButtonClass(card: Flashcard) {
    const cardId = getFlashcardId(card);

    if (isMatched(cardId)) {
      return matchedButtonClass;
    }

    if (mismatchPair?.termId === cardId) {
      return mismatchButtonClass;
    }

    if (selectedTermId === cardId) {
      return selectedButtonClass;
    }

    return baseButtonClass;
  }

  function getDefinitionButtonClass(card: Flashcard) {
    const cardId = getFlashcardId(card);

    if (isMatched(cardId)) {
      return matchedButtonClass;
    }

    if (mismatchPair?.definitionId === cardId) {
      return mismatchButtonClass;
    }

    if (selectedDefinitionId === cardId) {
      return selectedButtonClass;
    }

    return baseButtonClass;
  }

  if (flashcards.length < 2) {
    return (
      <GameEmptyState
        maxWidth="xl"
        message="Add at least 2 flashcards to start matching mode."
      />
    );
  }

  if (isFinished) {
    return (
      <GameResultCard
        title="Matching completed"
        scoreLabel="Your score"
        score={finalScore}
        total={cardsForRound.length}
        progressSuffix="correct"
        primaryActionLabel="Try again"
        onPrimaryAction={resetGame}
      >
        <div className="mt-4 flex justify-center">
          <GameTimer seconds={elapsedSeconds} />
        </div>

        <p className="mt-2 text-sm text-muted-foreground">
          Matched pairs: {matchedIds.length} / {cardsForRound.length}
        </p>

        <p className="mt-1 text-sm text-muted-foreground">
          Mistakes: {mistakes}
        </p>
      </GameResultCard>
    );
  }

  return (
    <GameShell maxWidth="xl">
      <GameProgress
        current={matchedIds.length}
        total={cardsForRound.length}
        suffix="matched"
      />

      <div className="flex justify-end">
        <GameTimer seconds={elapsedSeconds} />
      </div>

      <div key={roundId} className="game-enter space-y-6">
        <div className="rounded-2xl border border-pink-200 bg-pink-50/60 p-5 dark:border-pink-900 dark:bg-pink-950/20">
          <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <p className="text-sm text-muted-foreground">Matching mode</p>

              <h1 className="text-2xl font-bold text-foreground">
                Match terms with their definitions
              </h1>
            </div>

            <div className="text-sm text-muted-foreground">
              Mistakes:{" "}
              <span className="font-semibold text-pink-500 dark:text-pink-400">
                {mistakes}
              </span>
            </div>
          </div>
        </div>

        <div className="grid min-w-0 gap-6 md:grid-cols-2">
          <div className="min-w-0 space-y-3">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">
              Terms
            </h2>

            <div className="grid min-w-0 gap-3">
              {cardsForRound.map((card) => {
                const cardId = getFlashcardId(card);

                return (
                  <Button
                    key={`term-${cardId}`}
                    type="button"
                    variant="outline"
                    className={getTermButtonClass(card)}
                    disabled={isMatched(cardId) || Boolean(mismatchPair)}
                    onClick={() => handleTermClick(card)}
                  >
                    <span className="min-w-0 whitespace-normal break-words text-left">
                      {card.term}
                    </span>
                  </Button>
                );
              })}
            </div>
          </div>

          <div className="min-w-0 space-y-3">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">
              Definitions
            </h2>

            <div className="grid min-w-0 gap-3">
              {definitionCards.map((card) => {
                const cardId = getFlashcardId(card);

                return (
                  <Button
                    key={`definition-${cardId}`}
                    type="button"
                    variant="outline"
                    className={getDefinitionButtonClass(card)}
                    disabled={isMatched(cardId) || Boolean(mismatchPair)}
                    onClick={() => handleDefinitionClick(card)}
                  >
                    <span className="min-w-0 whitespace-normal break-words text-left">
                      {card.definition}
                    </span>
                  </Button>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    </GameShell>
  );
}
