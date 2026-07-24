"use client";

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
import { Input } from "@/components/ui/input";
import type { SaveGameQuestionResultRequest } from "@/types/games";

import { shuffleArray } from "./game-utils";

interface TypingGameProps {
  flashcards: Flashcard[];
  setId: number | string;
}

type AnswerStatus = "correct" | "incorrect" | "revealed" | null;

interface TypingGameState {
  questions: Flashcard[];
  currentIndex: number;
  userAnswer: string;
  answerStatus: AnswerStatus;
  score: number;
  questionResults: SaveGameQuestionResultRequest[];
  isResultSaved: boolean;
  startedAt: number;
  finishedAt: number | null;
}

function normalizeAnswer(value: string) {
  return value
    .trim()
    .toLowerCase()
    .replace(/[.,!?;:()"'`]/g, "")
    .replace(/\s+/g, " ");
}

function createNewTypingGame(flashcards: Flashcard[]): TypingGameState {
  return {
    questions: shuffleArray(flashcards),
    currentIndex: 0,
    userAnswer: "",
    answerStatus: null,
    score: 0,
    questionResults: [],
    isResultSaved: false,
    startedAt: Date.now(),
    finishedAt: null,
  };
}

export default function TypingGame({ flashcards, setId }: TypingGameProps) {
  const storageKey = getGameStorageKey(setId, "written");

  const [gameState, setGameState, clearGameState] =
    usePersistedGameState<TypingGameState>(storageKey, () =>
      createNewTypingGame(flashcards),
    );

  const {
    questions,
    currentIndex,
    userAnswer,
    answerStatus,
    score,
    questionResults = [],
    isResultSaved,
    startedAt,
    finishedAt,
  } = gameState;

  const isFinished = currentIndex >= questions.length;

  const currentQuestion = questions[currentIndex];

  const isAnswered = answerStatus !== null;

  const elapsedSeconds = useGameElapsedSeconds(startedAt, finishedAt);

  const durationSeconds = isFinished
    ? getElapsedGameSeconds(startedAt, finishedAt)
    : undefined;

  function updateGameState(nextState: Partial<TypingGameState>) {
    setGameState((previousState) => ({
      ...previousState,
      ...nextState,
    }));
  }

  useSaveGameResultOnFinish({
    setId,
    mode: "written",
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

    setGameState(createNewTypingGame(flashcards));
  }

  function createCurrentQuestionResult({
    userAnswerValue,
    wasCorrect,
  }: {
    userAnswerValue: string;
    wasCorrect: boolean;
  }) {
    if (!currentQuestion) {
      return null;
    }

    return createDefinitionAnswerQuestionResult({
      flashcard: currentQuestion,
      questionOrder: currentIndex,
      questionType: "written",
      userAnswer: userAnswerValue,
      wasCorrect,
    });
  }

  function handleCheckAnswer() {
    if (!currentQuestion || isAnswered) {
      return;
    }

    const normalizedUserAnswer = normalizeAnswer(userAnswer);

    const normalizedCorrectAnswer = normalizeAnswer(currentQuestion.definition);

    if (!normalizedUserAnswer) {
      return;
    }

    const isCorrect = normalizedUserAnswer === normalizedCorrectAnswer;

    const questionResult = createCurrentQuestionResult({
      userAnswerValue: userAnswer,
      wasCorrect: isCorrect,
    });

    updateGameState({
      answerStatus: isCorrect ? "correct" : "incorrect",
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
      userAnswerValue: "I don't know",
      wasCorrect: false,
    });

    updateGameState({
      answerStatus: "revealed",
      questionResults: questionResult
        ? [...questionResults, questionResult]
        : questionResults,
    });
  }

  function handleNext() {
    const nextIndex = currentIndex + 1;

    updateGameState({
      currentIndex: nextIndex,
      userAnswer: "",
      answerStatus: null,
      finishedAt: nextIndex >= questions.length ? Date.now() : finishedAt,
    });
  }

  if (flashcards.length < 1) {
    return (
      <GameEmptyState message="Add at least 1 flashcard to start written mode." />
    );
  }

  if (isFinished || !currentQuestion) {
    return (
      <GameResultCard
        title="Written mode completed"
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
              Type the correct answer for:
            </p>

            <h1 className="mt-2 wrap-break-word text-3xl font-bold text-foreground">
              {currentQuestion.term}
            </h1>
          </div>
        </div>

        <div className="space-y-3">
          <Input
            value={userAnswer}
            disabled={isAnswered}
            placeholder="Type your answer..."
            className="h-12 rounded-xl border-input bg-background text-foreground placeholder:text-muted-foreground focus-visible:border-pink-300 focus-visible:ring-pink-500/20 dark:focus-visible:border-pink-800"
            onChange={(event) =>
              updateGameState({
                userAnswer: event.target.value,
              })
            }
            onKeyDown={(event) => {
              if (event.key === "Enter") {
                if (isAnswered) {
                  handleNext();
                } else {
                  handleCheckAnswer();
                }
              }
            }}
          />

          {answerStatus === "correct" && (
            <div className="rounded-xl border border-green-200 bg-green-50 p-4 text-sm text-green-700 dark:border-green-900 dark:bg-green-950/30 dark:text-green-400">
              Correct! Great job.
            </div>
          )}

          {answerStatus === "incorrect" && (
            <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700 dark:border-red-900 dark:bg-red-950/30 dark:text-red-400">
              <p className="font-semibold">Not quite.</p>

              <p className="mt-1 wrap-break-word">
                Correct answer:{" "}
                <span className="font-semibold">
                  {currentQuestion.definition}
                </span>
              </p>
            </div>
          )}

          {answerStatus === "revealed" && (
            <div className="rounded-xl border border-pink-200 bg-pink-50 p-4 text-sm text-pink-700 dark:border-pink-900 dark:bg-pink-950/30 dark:text-pink-400">
              <p className="font-semibold">Correct answer:</p>

              <p className="mt-1 wrap-break-word">
                {currentQuestion.definition}
              </p>
            </div>
          )}
        </div>
      </div>

      {!isAnswered ? (
        <Button
          type="button"
          className="w-full bg-pink-500 text-white hover:bg-pink-600"
          disabled={!userAnswer.trim()}
          onClick={handleCheckAnswer}
        >
          Check answer
        </Button>
      ) : (
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
