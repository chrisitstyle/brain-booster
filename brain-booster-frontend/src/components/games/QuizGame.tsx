"use client";

import { useMemo, useState } from "react";
import type { Flashcard } from "@/api/flashcardService";
import { Button } from "@/components/ui/button";
import GameEmptyState from "@/components/games/shared/GameEmptyState";
import GameProgress from "@/components/games/shared/GameProgress";
import GameResultCard from "@/components/games/shared/GameResultCard";
import GameShell from "@/components/games/shared/GameShell";
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
  const isAnswered = selectedAnswer !== null;

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
        onPrimaryAction={() => {
          setCurrentIndex(0);
          setSelectedAnswer(null);
          setScore(0);
        }}
      />
    );
  }

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
    <GameShell>
      <GameProgress current={score} total={questions.length} suffix="correct" />

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
    </GameShell>
  );
}
