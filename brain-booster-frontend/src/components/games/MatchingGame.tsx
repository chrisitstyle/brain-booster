"use client";

import { useMemo, useState } from "react";
import type { Flashcard } from "@/api/flashcardService";
import { Button } from "@/components/ui/button";
import { shuffleArray } from "./game-utils";

interface MatchingGameProps {
  flashcards: Flashcard[];
}

interface MismatchPair {
  termId: string;
  definitionId: string;
}

function getFlashcardId(card: Flashcard) {
  return String(card.flashcardId);
}

export default function MatchingGame({ flashcards }: MatchingGameProps) {
  const [roundId, setRoundId] = useState(0);
  const [selectedTermId, setSelectedTermId] = useState<string | null>(null);
  const [selectedDefinitionId, setSelectedDefinitionId] = useState<
    string | null
  >(null);
  const [matchedIds, setMatchedIds] = useState<string[]>([]);
  const [mistakes, setMistakes] = useState(0);
  const [mismatchPair, setMismatchPair] = useState<MismatchPair | null>(null);

  const cardsForRound = useMemo(() => {
    return shuffleArray(flashcards).slice(0, Math.min(6, flashcards.length));
  }, [flashcards, roundId]);

  const definitionCards = useMemo(() => {
    return shuffleArray(cardsForRound);
  }, [cardsForRound]);

  const isFinished =
    cardsForRound.length > 0 && matchedIds.length === cardsForRound.length;

  const progressPercentage =
    cardsForRound.length > 0
      ? (matchedIds.length / cardsForRound.length) * 100
      : 0;

  function isMatched(cardId: string) {
    return matchedIds.includes(cardId);
  }

  function resetGame() {
    setRoundId((previousRoundId) => previousRoundId + 1);
    setSelectedTermId(null);
    setSelectedDefinitionId(null);
    setMatchedIds([]);
    setMistakes(0);
    setMismatchPair(null);
  }

  function checkPair(termId: string, definitionId: string) {
    if (termId === definitionId) {
      setMatchedIds((previousMatchedIds) => {
        if (previousMatchedIds.includes(termId)) {
          return previousMatchedIds;
        }

        return [...previousMatchedIds, termId];
      });

      setSelectedTermId(null);
      setSelectedDefinitionId(null);
      return;
    }

    setMistakes((previousMistakes) => previousMistakes + 1);
    setMismatchPair({ termId, definitionId });

    window.setTimeout(() => {
      setSelectedTermId(null);
      setSelectedDefinitionId(null);
      setMismatchPair(null);
    }, 600);
  }

  function handleTermClick(card: Flashcard) {
    const cardId = getFlashcardId(card);

    if (isMatched(cardId) || mismatchPair) return;

    setSelectedTermId(cardId);

    if (selectedDefinitionId) {
      checkPair(cardId, selectedDefinitionId);
    }
  }

  function handleDefinitionClick(card: Flashcard) {
    const cardId = getFlashcardId(card);

    if (isMatched(cardId) || mismatchPair) return;

    setSelectedDefinitionId(cardId);

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
    return "matching-shake h-auto min-h-14 w-full min-w-0 justify-start whitespace-normal break-words rounded-xl border-red-300 bg-red-50 px-4 py-3 text-left leading-relaxed text-red-700 transition disabled:opacity-100";
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
      <div className="mx-auto max-w-4xl rounded-2xl border border-pink-100 bg-white p-6 text-center text-gray-700 shadow-sm">
        Add at least 2 flashcards to start matching mode.
      </div>
    );
  }

  if (isFinished) {
    return (
      <div className="mx-auto max-w-2xl rounded-2xl border border-pink-100 bg-white p-6 text-center shadow-sm">
        <h1 className="text-2xl font-bold text-gray-800">Matching completed</h1>

        <p className="mt-4 text-lg text-gray-700">
          Matched pairs:{" "}
          <span className="font-semibold text-pink-500">
            {matchedIds.length} / {cardsForRound.length}
          </span>
        </p>

        <p className="mt-2 text-sm text-gray-500">Mistakes: {mistakes}</p>

        <div className="mt-6 space-y-2">
          <div className="flex items-center justify-between text-sm text-gray-500">
            <span>Progress</span>
            <span>
              {matchedIds.length} / {cardsForRound.length} matched
            </span>
          </div>

          <div className="h-2 w-full overflow-hidden rounded-full bg-gray-200">
            <div
              className="h-full rounded-full bg-pink-500 transition-all duration-500"
              style={{ width: `${progressPercentage}%` }}
            />
          </div>
        </div>

        <Button
          type="button"
          className="mt-6 bg-pink-500 text-white hover:bg-pink-600"
          onClick={resetGame}
        >
          Try again
        </Button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-4xl space-y-6 rounded-2xl border border-pink-100 bg-white p-6 shadow-sm">
      <div className="space-y-2">
        <div className="flex items-center justify-between text-sm text-gray-500">
          <span>Progress</span>
          <span>
            {matchedIds.length} / {cardsForRound.length} matched
          </span>
        </div>

        <div className="h-2 w-full overflow-hidden rounded-full bg-gray-200">
          <div
            className="h-full rounded-full bg-pink-500 transition-all duration-500"
            style={{ width: `${progressPercentage}%` }}
          />
        </div>
      </div>

      <div key={roundId} className="matching-game-enter space-y-6">
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

      <style jsx global>{`
        .matching-game-enter {
          animation: matching-game-enter 280ms ease-out;
        }

        .matching-shake {
          animation: matching-shake 260ms ease-in-out;
        }

        @keyframes matching-game-enter {
          from {
            opacity: 0;
            transform: translateY(12px) scale(0.98);
          }

          to {
            opacity: 1;
            transform: translateY(0) scale(1);
          }
        }

        @keyframes matching-shake {
          0% {
            transform: translateX(0);
          }

          25% {
            transform: translateX(-4px);
          }

          50% {
            transform: translateX(4px);
          }

          75% {
            transform: translateX(-3px);
          }

          100% {
            transform: translateX(0);
          }
        }
      `}</style>
    </div>
  );
}
