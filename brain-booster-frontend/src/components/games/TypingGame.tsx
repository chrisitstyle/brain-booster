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
import { createDefinitionAnswerQuestionResult } from "@/components/games/utils/gameQuestionResults";
import { Button } from "@/components/ui/button";
import type { SaveGameQuestionResultRequest } from "@/types/games";

import { buildQuizQuestions } from "./game-utils";

interface QuizGameProps {
  flashcards: Flashcard[];
  setId: number | string;
}

interface QuizGameState {
  currentIndex: number;
  selectedAnswer: string | null;
  score: number;
  questions: ReturnType<typeof buildQuizQuestions>;
  questionResults: SaveGameQuestionResultRequest[];
  isResultSaved: boolean;
  startedAt: number;
  finishedAt: number | null;
}

export default function QuizGame({ flashcards, setId }: QuizGameProps) {
  const storageKey = getGameStorageKey(setId, "multiple-choice");

  const initialQuestions = useMemo(
    () => buildQuizQuestions(flashcards),
    [flashcards],
  );

  const [gameState, setGameState, clearGameState] =
    usePersistedGameState<QuizGameState>(storageKey, () => ({
      currentIndex: 0,
      selectedAnswer: null,
      score: 0,
      questions: initialQuestions,
      questionResults: [],
      isResultSaved: false,
      startedAt: Date.now(),
      finishedAt: null,
    }));

  const {
    currentIndex,
    selectedAnswer,
    score,
    questions,
    questionResults,
    isResultSaved,
    startedAt,
    finishedAt,
  } = gameState;

  const isFinished = currentIndex >= questions.length;

  const currentQuestion = questions[currentIndex];

  const isAnswered = selectedAnswer !== null;

  const elapsedSeconds = useGameElapsedSeconds(startedAt, finishedAt);

  const durationSeconds = isFinished
    ? getElapsedGameSeconds(startedAt, finishedAt)
    : undefined;

  const currentFlashcard = currentQuestion
    ? flashcards.find(
        (flashcard) =>
          flashcard.term === currentQuestion.prompt &&
          flashcard.definition === currentQuestion.correctAnswer,
      )
    : undefined;

  function updateGameState(nextState: Partial<QuizGameState>) {
    setGameState((previousState) => ({
      ...previousState,
      ...nextState,
    }));
  }

  useSaveGameResultOnFinish({
    setId,
    mode: "multiple-choice",
    score,
    totalQuestions: questions.length,
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

  function restartGame() {
    clearGameState();

    setGameState({
      currentIndex: 0,
      selectedAnswer: null,
      score: 0,
      questions: buildQuizQuestions(flashcards),
      questionResults: [],
      isResultSaved: false,
      startedAt: Date.now(),
      finishedAt: null,
    });
  }

  function createCurrentQuestionResult({
    userAnswer,
    wasCorrect,
  }: {
    userAnswer: string;
    wasCorrect: boolean;
  }) {
    if (!currentFlashcard) {
      return null;
    }

    return createDefinitionAnswerQuestionResult({
      flashcard: currentFlashcard,
      questionOrder: currentIndex,
      questionType: "multiple-choice",
      userAnswer,
      wasCorrect,
    });
  }

  function handleAnswer(answer: string) {
    if (!currentQuestion || isAnswered) {
      return;
    }

    const isCorrect = answer === currentQuestion.correctAnswer;

    const questionResult = createCurrentQuestionResult({
      userAnswer: answer,
      wasCorrect: isCorrect,
    });

    updateGameState({
      selectedAnswer: answer,
      score: isCorrect ? score + 1 : score,
      questionResults: questionResult
        ? [...questionResults, questionResult]
        : questionResults,
    });
  }

  function handleDontKnow() {
    if (!currentQuestion || isAnswered) {
      return;
    }

    const questionResult = createCurrentQuestionResult({
      userAnswer: "I don't know",
      wasCorrect: false,
    });

    updateGameState({
      selectedAnswer: currentQuestion.correctAnswer,
      questionResults: questionResult
        ? [...questionResults, questionResult]
        : questionResults,
    });
  }

  function handleNext() {
    const nextIndex = currentIndex + 1;

    updateGameState({
      selectedAnswer: null,
      currentIndex: nextIndex,
      finishedAt: nextIndex >= questions.length ? Date.now() : finishedAt,
    });
  }

  if (flashcards.length < 2) {
    return (
      <GameEmptyState message="Add at least 2 flashcards to start multiple choice mode." />
    );
  }

  if (isFinished || !currentQuestion) {
    return (
      <GameResultCard
        title="Multiple choice completed"
        scoreLabel="Your score"
        score={score}
        total={questions.length}
        progressSuffix="correct"
        primaryActionLabel="Try again"
        onPrimaryAction={restartGame}
      >
        <div className="mt-4 flex justify-center">
          <GameTimer seconds={elapsedSeconds} />
        </div>
      </GameResultCard>
    );
  }

  return (
    <GameShell>
      <GameProgress current={score} total={questions.length} suffix="correct" />

      <div className="flex justify-end">
        <GameTimer seconds={elapsedSeconds} />
      </div>

      <div key={currentIndex} className="game-enter space-y-6">
        <div className="relative rounded-2xl border border-pink-200 bg-pink-50/60 p-5 dark:border-pink-900 dark:bg-pink-950/20">
          {!isAnswered && (
            <Button
              type="button"
              variant="ghost"
              size="sm"
              className="absolute right-3 top-3 h-8 px-3 text-xs font-medium text-pink-500 hover:bg-pink-100 hover:text-pink-600 dark:text-pink-400 dark:hover:bg-pink-950/50 dark:hover:text-pink-300"
              onClick={handleDontKnow}
            >
              Don&apos;t know?
            </Button>
          )}

          <div className="pr-28">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <span>
                Question {currentIndex + 1} of {questions.length}
              </span>

              <span aria-hidden="true">•</span>

              <span>Score: {score}</span>
            </div>

            <p className="mt-5 text-sm text-muted-foreground">
              What does this mean?
            </p>

            <h1 className="mt-2 break-words text-3xl font-bold text-foreground">
              {currentQuestion.prompt}
            </h1>
          </div>
        </div>

        <div className="grid gap-3">
          {currentQuestion.options.map((option) => {
            const isCorrect = option === currentQuestion.correctAnswer;

            const isSelected = option === selectedAnswer;

            let className =
              "h-auto min-h-12 justify-start whitespace-normal break-words border-border bg-background text-left text-foreground transition-colors hover:border-pink-300 hover:bg-pink-50 hover:text-pink-600 dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-400";

            if (isAnswered && isCorrect) {
              className =
                "h-auto min-h-12 justify-start whitespace-normal break-words border-green-500 bg-green-50 text-left text-green-700 hover:bg-green-50 hover:text-green-700 dark:border-green-800 dark:bg-green-950/30 dark:text-green-400 dark:hover:bg-green-950/30 dark:hover:text-green-400";
            }

            if (isAnswered && isSelected && !isCorrect) {
              className =
                "h-auto min-h-12 justify-start whitespace-normal break-words border-red-500 bg-red-50 text-left text-red-700 hover:bg-red-50 hover:text-red-700 dark:border-red-800 dark:bg-red-950/30 dark:text-red-400 dark:hover:bg-red-950/30 dark:hover:text-red-400";
            }

            return (
              <Button
                key={option}
                type="button"
                variant="outline"
                className={className}
                disabled={isAnswered}
                onClick={() => handleAnswer(option)}
              >
                {option}
              </Button>
            );
          })}
        </div>
      </div>

      {isAnswered && (
        <Button
          type="button"
          className="w-full bg-pink-500 text-white hover:bg-pink-600"
          onClick={handleNext}
        >
          Next question
        </Button>
      )}
    </GameShell>
  );
}
