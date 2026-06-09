"use client";

import type { Flashcard } from "@/api/flashcardService";
import { Button } from "@/components/ui/button";
import GameEmptyState from "@/components/games/shared/GameEmptyState";
import GameProgress from "@/components/games/shared/GameProgress";
import GameResultCard from "@/components/games/shared/GameResultCard";
import GameShell from "@/components/games/shared/GameShell";
import { useSaveGameResultOnFinish } from "@/components/games/hooks/useSaveGameResultOnFinish";
import { usePersistedGameState } from "@/components/games/shared/usePersistedGameState";
import { getGameStorageKey } from "@/components/games/shared/game-storage";
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
  mismatchPair: MismatchPair | null;
  isResultSaved: boolean;
}

function getFlashcardId(card: Flashcard) {
  return String(card.flashcardId);
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
    mismatchPair: null,
    isResultSaved: false,
  };
}

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
    mismatchPair,
    isResultSaved,
  } = gameState;

  const isFinished =
    cardsForRound.length > 0 && matchedIds.length === cardsForRound.length;

  const finalScore = Math.max(cardsForRound.length - mistakes, 0);

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
      updateGameState({
        matchedIds: matchedIds.includes(termId)
          ? matchedIds
          : [...matchedIds, termId],
        selectedTermId: null,
        selectedDefinitionId: null,
      });

      return;
    }

    updateGameState({
      mistakes: mistakes + 1,
      mismatchPair: { termId, definitionId },
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

    if (isMatched(cardId) || mismatchPair) return;

    updateGameState({
      selectedTermId: cardId,
    });

    if (selectedDefinitionId) {
      checkPair(cardId, selectedDefinitionId);
    }
  }

  function handleDefinitionClick(card: Flashcard) {
    const cardId = getFlashcardId(card);

    if (isMatched(cardId) || mismatchPair) return;

    updateGameState({
      selectedDefinitionId: cardId,
    });

    if (selectedTermId) {
      checkPair(selectedTermId, cardId);
    }
  }

  function getBaseButtonClass() {
    return "h-auto min-h-14 w-full min-w-0 justify-start whitespace-normal break-words rounded-xl border-gray-200 bg-white px-4 py-3 text-left leading-relaxed text-gray-700 transition hover:border-pink-300 hover:bg-pink-50 hover:text-pink-600 disabled:opacity-100";
  }

  function getSelectedButtonClass() {
    return "h-auto min-h-14 w-full min-w-0 justify-start whitespace-normal break-words rounded-xl border-pink-400 bg-pink-50 px-4 py-3 text-left leading-relaxed text-pink-600 ring-2 ring-pink-100 transition disabled:opacity-100";
  }

  function getMismatchButtonClass() {
    return "game-shake h-auto min-h-14 w-full min-w-0 justify-start whitespace-normal break-words rounded-xl border-red-300 bg-red-50 px-4 py-3 text-left leading-relaxed text-red-700 transition disabled:opacity-100";
  }

  function getMatchedButtonClass() {
    return "h-auto min-h-14 w-full min-w-0 justify-start whitespace-normal break-words rounded-xl border-green-200 bg-green-50 px-4 py-3 text-left leading-relaxed text-green-700 transition hover:bg-green-50 hover:text-green-700 disabled:opacity-100";
  }

  function getTermButtonClass(card: Flashcard) {
    const cardId = getFlashcardId(card);

    if (isMatched(cardId)) {
      return getMatchedButtonClass();
    }

    if (mismatchPair?.termId === cardId) {
      return getMismatchButtonClass();
    }

    if (selectedTermId === cardId) {
      return getSelectedButtonClass();
    }

    return getBaseButtonClass();
  }

  function getDefinitionButtonClass(card: Flashcard) {
    const cardId = getFlashcardId(card);

    if (isMatched(cardId)) {
      return getMatchedButtonClass();
    }

    if (mismatchPair?.definitionId === cardId) {
      return getMismatchButtonClass();
    }

    if (selectedDefinitionId === cardId) {
      return getSelectedButtonClass();
    }

    return getBaseButtonClass();
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
        <p className="mt-2 text-sm text-gray-500">
          Matched pairs: {matchedIds.length} / {cardsForRound.length}
        </p>
        <p className="mt-1 text-sm text-gray-500">Mistakes: {mistakes}</p>
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

      <div key={roundId} className="game-enter space-y-6">
        <div className="rounded-2xl border border-pink-100 bg-pink-50/40 p-5">
          <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <p className="text-sm text-gray-500">Matching mode</p>

              <h1 className="text-2xl font-bold text-gray-800">
                Match terms with their definitions
              </h1>
            </div>

            <div className="text-sm text-gray-500">
              Mistakes:{" "}
              <span className="font-semibold text-pink-500">{mistakes}</span>
            </div>
          </div>
        </div>

        <div className="grid min-w-0 gap-6 md:grid-cols-2">
          <div className="min-w-0 space-y-3">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-gray-500">
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
            <h2 className="text-sm font-semibold uppercase tracking-wide text-gray-500">
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
