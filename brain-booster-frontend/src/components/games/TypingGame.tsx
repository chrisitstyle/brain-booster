"use client";

import type { Flashcard } from "@/api/flashcardService";
import type { SaveGameQuestionResultRequest } from "@/api/gameResultService";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import GameEmptyState from "@/components/games/shared/GameEmptyState";
import GameProgress from "@/components/games/shared/GameProgress";
import GameResultCard from "@/components/games/shared/GameResultCard";
import GameShell from "@/components/games/shared/GameShell";
import GameTimer from "@/components/games/shared/GameTimer";
import { useSaveGameResultOnFinish } from "@/components/games/hooks/useSaveGameResultOnFinish";
import {
  getElapsedGameSeconds,
  useGameElapsedSeconds,
} from "@/components/games/hooks/useGameElapsedSeconds";
import { usePersistedGameState } from "@/components/games/shared/usePersistedGameState";
import { getGameStorageKey } from "@/components/games/shared/game-storage";
import { createDefinitionAnswerQuestionResult } from "@/components/games/utils/gameQuestionResults";
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
    if (!currentQuestion) return null;

    return createDefinitionAnswerQuestionResult({
      flashcard: currentQuestion,
      questionOrder: currentIndex,
      questionType: "written",
      userAnswer: userAnswerValue,
      wasCorrect,
    });
  }

  function handleCheckAnswer() {
    if (!currentQuestion || isAnswered) return;

    const normalizedUserAnswer = normalizeAnswer(userAnswer);
    const normalizedCorrectAnswer = normalizeAnswer(currentQuestion.definition);

    if (!normalizedUserAnswer) return;

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
    if (!currentQuestion || isAnswered) return;

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
        {" "}
        <div className="mt-4 flex justify-center">
          {" "}
          <GameTimer seconds={elapsedSeconds} />{" "}
        </div>{" "}
      </GameResultCard>
    );
  }

  return (
    <GameShell>
      {" "}
      <GameProgress current={score} total={questions.length} suffix="correct" />
      <div className="flex justify-end">
        <GameTimer seconds={elapsedSeconds} />
      </div>
      <div key={currentIndex} className="game-enter space-y-6">
        <div className="relative rounded-2xl border border-pink-100 bg-pink-50/40 p-5">
          {!isAnswered && (
            <Button
              type="button"
              variant="ghost"
              size="sm"
              className="absolute right-3 top-3 h-8 px-3 text-xs font-medium text-pink-500 hover:bg-pink-100 hover:text-pink-600"
              onClick={handleDontKnow}
            >
              Don&apos;t know?
            </Button>
          )}

          <div className="pr-28">
            <div className="flex items-center gap-2 text-sm text-gray-500">
              <span>
                Question {currentIndex + 1} of {questions.length}
              </span>
              <span>•</span>
              <span>Score: {score}</span>
            </div>

            <p className="mt-5 text-sm text-gray-500">
              Type the correct answer for:
            </p>

            <h1 className="mt-2 text-3xl font-bold text-gray-800">
              {currentQuestion.term}
            </h1>
          </div>
        </div>

        <div className="space-y-3">
          <Input
            value={userAnswer}
            disabled={isAnswered}
            placeholder="Type your answer..."
            className="h-12 rounded-xl border-gray-200 bg-white text-gray-800 placeholder:text-gray-400 focus-visible:ring-pink-400"
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
            <div className="rounded-xl border border-green-200 bg-green-50 p-4 text-sm text-green-700">
              Correct! Great job.
            </div>
          )}

          {answerStatus === "incorrect" && (
            <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
              <p className="font-semibold">Not quite.</p>
              <p className="mt-1">
                Correct answer:{" "}
                <span className="font-semibold">
                  {currentQuestion.definition}
                </span>
              </p>
            </div>
          )}

          {answerStatus === "revealed" && (
            <div className="rounded-xl border border-pink-200 bg-pink-50 p-4 text-sm text-pink-700">
              <p className="font-semibold">Correct answer:</p>
              <p className="mt-1">{currentQuestion.definition}</p>
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
