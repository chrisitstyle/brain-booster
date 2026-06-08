"use client";

import { useMemo, useState } from "react";
import type { Flashcard } from "@/api/flashcardService";
import { Button } from "@/components/ui/button";
import { buildQuizQuestions } from "./game-utils";

interface QuizGameProps {
  flashcards: Flashcard[];
}

export default function QuizGame({ flashcards }: QuizGameProps) {
  const questions = useMemo(() => buildQuizQuestions(flashcards), [flashcards]);

  const [currentIndex, setCurrentIndex] = useState(0);
  const [selectedAnswer, setSelectedAnswer] = useState<string | null>(null);
  const [score, setScore] = useState(0);

  const isFinished = currentIndex >= questions.length;
  const currentQuestion = questions[currentIndex];

  const progressPercentage =
    questions.length > 0 ? (score / questions.length) * 100 : 0;

  if (flashcards.length < 2) {
    return (
      <div className="mx-auto max-w-2xl rounded-2xl border border-pink-100 bg-white p-6 text-center text-gray-700 shadow-sm">
        Add at least 2 flashcards to start the quiz.
      </div>
    );
  }

  if (isFinished || !currentQuestion) {
    return (
      <div className="mx-auto max-w-2xl rounded-2xl border border-pink-100 bg-white p-6 text-center shadow-sm">
        <h1 className="text-2xl font-bold text-gray-800">Quiz completed</h1>

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
          className="mt-6 bg-pink-500 text-white hover:bg-pink-600"
          onClick={() => {
            setCurrentIndex(0);
            setSelectedAnswer(null);
            setScore(0);
          }}
        >
          Try again
        </Button>

        <style jsx>{`
          div {
            animation: quiz-fade-in 260ms ease-out;
          }

          @keyframes quiz-fade-in {
            from {
              opacity: 0;
              transform: translateY(10px) scale(0.98);
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

  const isAnswered = selectedAnswer !== null;

  function handleAnswer(answer: string) {
    if (isAnswered) return;

    setSelectedAnswer(answer);

    if (answer === currentQuestion.correctAnswer) {
      setScore((previousScore) => previousScore + 1);
    }
  }

  function handleDontKnow() {
    if (isAnswered) return;

    setSelectedAnswer(currentQuestion.correctAnswer);
  }

  function handleNext() {
    setSelectedAnswer(null);
    setCurrentIndex((previousIndex) => previousIndex + 1);
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

      <div key={currentIndex} className="quiz-question-enter space-y-6">
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

            <p className="mt-5 text-sm text-gray-500">What does this mean?</p>

            <h1 className="mt-2 text-3xl font-bold text-gray-800">
              {currentQuestion.prompt}
            </h1>
          </div>
        </div>

        <div className="grid gap-3">
          {currentQuestion.options.map((option) => {
            const isCorrect = option === currentQuestion.correctAnswer;
            const isSelected = option === selectedAnswer;

            let className =
              "justify-start border-gray-200 bg-white text-gray-700 transition hover:border-pink-300 hover:bg-pink-50 hover:text-pink-600";

            if (isAnswered && isCorrect) {
              className =
                "justify-start border-green-500 bg-green-50 text-green-700 hover:bg-green-50 hover:text-green-700";
            }

            if (isAnswered && isSelected && !isCorrect) {
              className =
                "justify-start border-red-500 bg-red-50 text-red-700 hover:bg-red-50 hover:text-red-700";
            }

            return (
              <Button
                key={option}
                type="button"
                variant="outline"
                className={className}
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

      <style jsx>{`
        .quiz-question-enter {
          animation: quiz-card-enter 280ms ease-out;
        }

        @keyframes quiz-card-enter {
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
