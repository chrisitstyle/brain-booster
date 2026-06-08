"use client";

import { useMemo, useState } from "react";
import type { Flashcard } from "@/api/flashcardService";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { shuffleArray } from "./game-utils";

interface TypingGameProps {
  flashcards: Flashcard[];
}

type AnswerStatus = "correct" | "incorrect" | "revealed" | null;

function normalizeAnswer(value: string) {
  return value
    .trim()
    .toLowerCase()
    .replace(/[.,!?;:()"'`]/g, "")
    .replace(/\s+/g, " ");
}

export default function TypingGame({ flashcards }: TypingGameProps) {
  const questions = useMemo(() => shuffleArray(flashcards), [flashcards]);

  const [currentIndex, setCurrentIndex] = useState(0);
  const [userAnswer, setUserAnswer] = useState("");
  const [answerStatus, setAnswerStatus] = useState<AnswerStatus>(null);
  const [score, setScore] = useState(0);

  const isFinished = currentIndex >= questions.length;
  const currentQuestion = questions[currentIndex];

  const progressPercentage =
    questions.length > 0 ? (score / questions.length) * 100 : 0;

  const isAnswered = answerStatus !== null;

  if (flashcards.length < 1) {
    return (
      <div className="mx-auto max-w-2xl rounded-2xl border border-pink-100 bg-white p-6 text-center text-gray-700 shadow-sm">
        Add at least 1 flashcard to start typing mode.
      </div>
    );
  }

  if (isFinished || !currentQuestion) {
    return (
      <div className="mx-auto max-w-2xl rounded-2xl border border-pink-100 bg-white p-6 text-center shadow-sm">
        <h1 className="text-2xl font-bold text-gray-800">
          Typing mode completed
        </h1>

        <p className="mt-4 text-lg text-gray-700">
          Your score:{" "}
          <span className="font-semibold text-pink-500">
            {score} / {questions.length}
          </span>
        </p>

        <div className="mt-6 space-y-2">
          <div className="flex items-center justify-between text-sm text-gray-500">
            <span>Progress</span>
            <span>
              {score} / {questions.length} correct
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
          onClick={() => {
            setCurrentIndex(0);
            setUserAnswer("");
            setAnswerStatus(null);
            setScore(0);
          }}
        >
          Try again
        </Button>
      </div>
    );
  }

  function handleCheckAnswer() {
    if (isAnswered) return;

    const normalizedUserAnswer = normalizeAnswer(userAnswer);
    const normalizedCorrectAnswer = normalizeAnswer(currentQuestion.definition);

    if (!normalizedUserAnswer) return;

    if (normalizedUserAnswer === normalizedCorrectAnswer) {
      setAnswerStatus("correct");
      setScore((previousScore) => previousScore + 1);
    } else {
      setAnswerStatus("incorrect");
    }
  }

  function handleDontKnow() {
    if (isAnswered) return;

    setAnswerStatus("revealed");
  }

  function handleNext() {
    setCurrentIndex((previousIndex) => previousIndex + 1);
    setUserAnswer("");
    setAnswerStatus(null);
  }

  return (
    <div className="mx-auto max-w-2xl space-y-6 rounded-2xl border border-pink-100 bg-white p-6 shadow-sm">
      <div className="space-y-2">
        <div className="flex items-center justify-between text-sm text-gray-500">
          <span>Progress</span>
          <span>
            {score} / {questions.length} correct
          </span>
        </div>

        <div className="h-2 w-full overflow-hidden rounded-full bg-gray-200">
          <div
            className="h-full rounded-full bg-pink-500 transition-all duration-500"
            style={{ width: `${progressPercentage}%` }}
          />
        </div>
      </div>

      <div key={currentIndex} className="typing-question-enter space-y-6">
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
            onChange={(event) => setUserAnswer(event.target.value)}
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

      <style jsx>{`
        .typing-question-enter {
          animation: typing-card-enter 280ms ease-out;
        }

        @keyframes typing-card-enter {
          from {
            opacity: 0;
            transform: translateY(12px) scale(0.98);
          }

          to {
            opacity: 1;
            transform: translateY(0) scale(1);
          }
        }
      `}</style>
    </div>
  );
}
