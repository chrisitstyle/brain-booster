"use client";

import { useMemo, useState } from "react";
import type { Flashcard } from "@/api/flashcardService";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { shuffleArray } from "./game-utils";

type AnswerWith = "term" | "definition" | "both";
type TestQuestionType = "trueFalse" | "multipleChoice" | "matching" | "written";

interface TestGameProps {
  flashcards: Flashcard[];
}

interface TestConfig {
  questionCount: number;
  answerWith: AnswerWith;
  enabledTypes: Record<TestQuestionType, boolean>;
}

interface TestQuestion {
  id: string;
  type: TestQuestionType;
  prompt: string;
  correctAnswer: string;
  options?: string[];
  trueFalseAnswer?: string;
  isTrueStatement?: boolean;
  matchingCards?: Flashcard[];
  answerWith: Exclude<AnswerWith, "both">;
}

function normalizeAnswer(value: string) {
  return value
    .trim()
    .toLowerCase()
    .replace(/[.,!?;:()"'`]/g, "")
    .replace(/\s+/g, " ");
}

function getQuestionSide(answerWith: AnswerWith): Exclude<AnswerWith, "both"> {
  if (answerWith === "both") {
    return Math.random() > 0.5 ? "term" : "definition";
  }

  return answerWith;
}

function getPromptAndAnswer(
  card: Flashcard,
  answerWith: Exclude<AnswerWith, "both">,
) {
  if (answerWith === "term") {
    return {
      prompt: card.definition,
      correctAnswer: card.term,
    };
  }

  return {
    prompt: card.term,
    correctAnswer: card.definition,
  };
}

function getAnswerValue(
  card: Flashcard,
  answerWith: Exclude<AnswerWith, "both">,
) {
  return answerWith === "term" ? card.term : card.definition;
}

function getPromptValue(
  card: Flashcard,
  answerWith: Exclude<AnswerWith, "both">,
) {
  return answerWith === "term" ? card.definition : card.term;
}

function buildTestQuestions(
  flashcards: Flashcard[],
  config: TestConfig,
): TestQuestion[] {
  const enabledTypes = Object.entries(config.enabledTypes)
    .filter(([, isEnabled]) => isEnabled)
    .map(([type]) => type as TestQuestionType);

  const selectedCards = shuffleArray(flashcards).slice(
    0,
    Math.min(config.questionCount, flashcards.length),
  );

  return selectedCards.map((card, index) => {
    const type = enabledTypes[index % enabledTypes.length];
    const answerWith = getQuestionSide(config.answerWith);
    const { prompt, correctAnswer } = getPromptAndAnswer(card, answerWith);

    if (type === "multipleChoice") {
      const wrongOptions = shuffleArray(
        flashcards
          .filter((flashcard) => flashcard.flashcardId !== card.flashcardId)
          .map((flashcard) => getAnswerValue(flashcard, answerWith)),
      ).slice(0, 3);

      return {
        id: `${type}-${card.flashcardId}-${index}`,
        type,
        prompt,
        correctAnswer,
        options: shuffleArray([correctAnswer, ...wrongOptions]),
        answerWith,
      };
    }

    if (type === "trueFalse") {
      const isTrueStatement = Math.random() > 0.5;

      const wrongCard =
        shuffleArray(
          flashcards.filter(
            (flashcard) => flashcard.flashcardId !== card.flashcardId,
          ),
        )[0] ?? card;

      const trueFalseAnswer = isTrueStatement
        ? correctAnswer
        : getAnswerValue(wrongCard, answerWith);

      return {
        id: `${type}-${card.flashcardId}-${index}`,
        type,
        prompt,
        correctAnswer,
        trueFalseAnswer,
        isTrueStatement,
        answerWith,
      };
    }

    if (type === "matching") {
      const matchingCards = shuffleArray(flashcards).slice(
        0,
        Math.min(4, flashcards.length),
      );

      return {
        id: `${type}-${card.flashcardId}-${index}`,
        type,
        prompt,
        correctAnswer,
        matchingCards,
        answerWith,
      };
    }

    return {
      id: `${type}-${card.flashcardId}-${index}`,
      type,
      prompt,
      correctAnswer,
      answerWith,
    };
  });
}

export default function TestGame({ flashcards }: TestGameProps) {
  const maxQuestions = flashcards.length;

  const [config, setConfig] = useState<TestConfig>({
    questionCount: Math.min(20, Math.max(1, maxQuestions)),
    answerWith: "both",
    enabledTypes: {
      trueFalse: false,
      multipleChoice: true,
      matching: false,
      written: false,
    },
  });

  const [questions, setQuestions] = useState<TestQuestion[]>([]);
  const [hasStarted, setHasStarted] = useState(false);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [score, setScore] = useState(0);

  const [selectedAnswer, setSelectedAnswer] = useState<string | null>(null);
  const [writtenAnswer, setWrittenAnswer] = useState("");
  const [isAnswered, setIsAnswered] = useState(false);

  const [selectedPromptId, setSelectedPromptId] = useState<number | null>(null);
  const [selectedAnswerId, setSelectedAnswerId] = useState<number | null>(null);
  const [matchedIds, setMatchedIds] = useState<number[]>([]);
  const [matchingMistakes, setMatchingMistakes] = useState(0);
  const [mismatchIds, setMismatchIds] = useState<number[]>([]);

  const currentQuestion = questions[currentIndex];
  const isFinished = hasStarted && currentIndex >= questions.length;

  const enabledTypesCount = Object.values(config.enabledTypes).filter(
    Boolean,
  ).length;

  const progressPercentage =
    questions.length > 0 ? (currentIndex / questions.length) * 100 : 0;

  const matchingPromptCards = useMemo(() => {
    if (!currentQuestion?.matchingCards) return [];
    return currentQuestion.matchingCards;
  }, [currentQuestion]);

  const matchingAnswerCards = useMemo(() => {
    if (!currentQuestion?.matchingCards) return [];
    return shuffleArray(currentQuestion.matchingCards);
  }, [currentQuestion]);

  function updateQuestionCount(value: string) {
    const numberValue = Number(value);

    if (Number.isNaN(numberValue)) return;

    setConfig((previousConfig) => ({
      ...previousConfig,
      questionCount: Math.min(Math.max(numberValue, 1), maxQuestions),
    }));
  }

  function toggleQuestionType(type: TestQuestionType) {
    setConfig((previousConfig) => ({
      ...previousConfig,
      enabledTypes: {
        ...previousConfig.enabledTypes,
        [type]: !previousConfig.enabledTypes[type],
      },
    }));
  }

  function startTest() {
    if (enabledTypesCount === 0) return;

    const nextQuestions = buildTestQuestions(flashcards, config);

    setQuestions(nextQuestions);
    setHasStarted(true);
    setCurrentIndex(0);
    setScore(0);
    resetQuestionState();
  }

  function resetQuestionState() {
    setSelectedAnswer(null);
    setWrittenAnswer("");
    setIsAnswered(false);
    setSelectedPromptId(null);
    setSelectedAnswerId(null);
    setMatchedIds([]);
    setMatchingMistakes(0);
    setMismatchIds([]);
  }

  function goToNextQuestion() {
    resetQuestionState();
    setCurrentIndex((previousIndex) => previousIndex + 1);
  }

  function restartSetup() {
    setHasStarted(false);
    setQuestions([]);
    setCurrentIndex(0);
    setScore(0);
    resetQuestionState();
  }

  function handleAnswer(answer: string) {
    if (!currentQuestion || isAnswered) return;

    setSelectedAnswer(answer);
    setIsAnswered(true);

    if (answer === currentQuestion.correctAnswer) {
      setScore((previousScore) => previousScore + 1);
    }
  }

  function handleTrueFalseAnswer(answer: boolean) {
    if (!currentQuestion || isAnswered) return;

    const isCorrect = answer === currentQuestion.isTrueStatement;

    setSelectedAnswer(answer ? "True" : "False");
    setIsAnswered(true);

    if (isCorrect) {
      setScore((previousScore) => previousScore + 1);
    }
  }

  function handleWrittenCheck() {
    if (!currentQuestion || isAnswered || !writtenAnswer.trim()) return;

    const isCorrect =
      normalizeAnswer(writtenAnswer) ===
      normalizeAnswer(currentQuestion.correctAnswer);

    setIsAnswered(true);

    if (isCorrect) {
      setScore((previousScore) => previousScore + 1);
    }
  }

  function isMatchingCompleted() {
    return (
      currentQuestion?.type === "matching" &&
      currentQuestion.matchingCards &&
      matchedIds.length === currentQuestion.matchingCards.length
    );
  }

  function handleMatchingPromptClick(cardId: number) {
    if (matchedIds.includes(cardId) || mismatchIds.length > 0) return;

    setSelectedPromptId(cardId);

    if (selectedAnswerId !== null) {
      checkMatchingPair(cardId, selectedAnswerId);
    }
  }

  function handleMatchingAnswerClick(cardId: number) {
    if (matchedIds.includes(cardId) || mismatchIds.length > 0) return;

    setSelectedAnswerId(cardId);

    if (selectedPromptId !== null) {
      checkMatchingPair(selectedPromptId, cardId);
    }
  }

  function checkMatchingPair(promptId: number, answerId: number) {
    if (promptId === answerId) {
      setMatchedIds((previousMatchedIds) => [...previousMatchedIds, promptId]);
      setSelectedPromptId(null);
      setSelectedAnswerId(null);
      return;
    }

    setMatchingMistakes((previousMistakes) => previousMistakes + 1);
    setMismatchIds([promptId, answerId]);

    window.setTimeout(() => {
      setSelectedPromptId(null);
      setSelectedAnswerId(null);
      setMismatchIds([]);
    }, 550);
  }

  function finishMatchingQuestion() {
    if (matchingMistakes === 0) {
      setScore((previousScore) => previousScore + 1);
    }

    goToNextQuestion();
  }

  if (flashcards.length < 2) {
    return (
      <div className="mx-auto max-w-2xl rounded-2xl border border-pink-100 bg-white p-6 text-center text-gray-700 shadow-sm">
        Add at least 2 flashcards to start test mode.
      </div>
    );
  }

  if (!hasStarted) {
    return (
      <div className="mx-auto max-w-2xl rounded-2xl border border-pink-100 bg-white p-6 shadow-sm">
        <div className="mb-8">
          <p className="text-sm font-semibold text-pink-500">Test mode</p>
          <h1 className="mt-1 text-3xl font-bold text-gray-800">
            Set up your test
          </h1>
          <p className="mt-2 text-sm text-gray-500">
            Choose question types and how answers should be shown.
          </p>
        </div>

        <div className="space-y-6">
          <div className="flex items-center justify-between gap-4">
            <label className="font-semibold text-gray-800">
              Questions{" "}
              <span className="text-sm font-normal text-gray-500">
                (max {maxQuestions})
              </span>
            </label>

            <Input
              type="number"
              min={1}
              max={maxQuestions}
              value={config.questionCount}
              className="h-12 w-24 rounded-xl border-pink-100 text-center font-semibold focus-visible:ring-pink-400"
              onChange={(event) => updateQuestionCount(event.target.value)}
            />
          </div>

          <div className="flex items-center justify-between gap-4 border-b border-gray-200 pb-5">
            <label className="font-semibold text-gray-800">Answer with</label>

            <select
              value={config.answerWith}
              className="h-11 rounded-full border border-pink-100 bg-white px-4 text-sm font-semibold text-gray-700 outline-none transition hover:border-pink-200 focus:border-pink-400"
              onChange={(event) =>
                setConfig((previousConfig) => ({
                  ...previousConfig,
                  answerWith: event.target.value as AnswerWith,
                }))
              }
            >
              <option value="term">Term</option>
              <option value="definition">Definition</option>
              <option value="both">Both</option>
            </select>
          </div>

          {[
            ["trueFalse", "True/False"],
            ["multipleChoice", "Multiple choice"],
            ["matching", "Matching"],
            ["written", "Written"],
          ].map(([type, label]) => {
            const questionType = type as TestQuestionType;
            const isEnabled = config.enabledTypes[questionType];

            return (
              <div
                key={type}
                className="flex items-center justify-between gap-4"
              >
                <span className="font-semibold text-gray-800">{label}</span>

                <button
                  type="button"
                  className={`relative h-7 w-12 rounded-full transition ${
                    isEnabled ? "bg-pink-500" : "bg-gray-300"
                  }`}
                  onClick={() => toggleQuestionType(questionType)}
                >
                  <span
                    className={`absolute top-1 h-5 w-5 rounded-full bg-white shadow-sm transition ${
                      isEnabled ? "left-6" : "left-1"
                    }`}
                  />
                </button>
              </div>
            );
          })}
        </div>

        {enabledTypesCount === 0 && (
          <p className="mt-6 rounded-xl border border-red-100 bg-red-50 p-3 text-sm text-red-600">
            Select at least one question type.
          </p>
        )}

        <div className="mt-8 flex justify-end">
          <Button
            type="button"
            className="rounded-full bg-pink-500 px-6 text-white hover:bg-pink-600"
            disabled={enabledTypesCount === 0}
            onClick={startTest}
          >
            Start test
          </Button>
        </div>
      </div>
    );
  }

  if (isFinished) {
    return (
      <div className="mx-auto max-w-2xl rounded-2xl border border-pink-100 bg-white p-6 text-center shadow-sm">
        <h1 className="text-2xl font-bold text-gray-800">Test completed</h1>

        <p className="mt-4 text-lg text-gray-700">
          Your score:{" "}
          <span className="font-semibold text-pink-500">
            {score} / {questions.length}
          </span>
        </p>

        <div className="mt-6 grid grid-cols-1 gap-3 sm:grid-cols-2">
          <Button
            type="button"
            variant="outline"
            className="w-full border-pink-200 text-pink-500 hover:bg-pink-50"
            onClick={restartSetup}
          >
            Change settings
          </Button>

          <Button
            type="button"
            className="w-full bg-pink-500 text-white hover:bg-pink-600"
            onClick={startTest}
          >
            Try again
          </Button>
        </div>
      </div>
    );
  }

  if (!currentQuestion) return null;

  return (
    <div className="mx-auto max-w-3xl space-y-6 rounded-2xl border border-pink-100 bg-white p-6 shadow-sm">
      <div className="space-y-2">
        <div className="flex items-center justify-between text-sm text-gray-500">
          <span>Progress</span>
          <span>
            {currentIndex} / {questions.length} answered
          </span>
        </div>

        <div className="h-2 w-full overflow-hidden rounded-full bg-gray-200">
          <div
            className="h-full rounded-full bg-pink-500 transition-all duration-500"
            style={{ width: `${progressPercentage}%` }}
          />
        </div>
      </div>

      <div key={currentQuestion.id} className="test-question-enter space-y-6">
        <div className="rounded-2xl border border-pink-100 bg-pink-50/40 p-5">
          <div className="flex items-center justify-between gap-4 text-sm text-gray-500">
            <span>
              Question {currentIndex + 1} of {questions.length}
            </span>
            <span className="font-medium capitalize">
              {currentQuestion.type.replace(/([A-Z])/g, " $1")}
            </span>
          </div>

          {currentQuestion.type !== "matching" && (
            <>
              <p className="mt-5 text-sm text-gray-500">
                Answer with {currentQuestion.answerWith}
              </p>

              <h1 className="mt-2 text-2xl font-bold text-gray-800">
                {currentQuestion.prompt}
              </h1>
            </>
          )}
        </div>

        {currentQuestion.type === "multipleChoice" && (
          <div className="grid gap-3">
            {currentQuestion.options?.map((option) => {
              const isCorrect = option === currentQuestion.correctAnswer;
              const isSelected = selectedAnswer === option;

              let className =
                "h-auto min-h-12 justify-start whitespace-normal border-gray-200 bg-white text-left text-gray-700 hover:border-pink-300 hover:bg-pink-50 hover:text-pink-600";

              if (isAnswered && isCorrect) {
                className =
                  "h-auto min-h-12 justify-start whitespace-normal border-green-300 bg-green-50 text-left text-green-700";
              }

              if (isAnswered && isSelected && !isCorrect) {
                className =
                  "h-auto min-h-12 justify-start whitespace-normal border-red-300 bg-red-50 text-left text-red-700";
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
        )}

        {currentQuestion.type === "trueFalse" && (
          <div className="space-y-4">
            <div className="rounded-xl border border-gray-200 bg-white p-4 text-gray-700">
              <span className="font-semibold">{currentQuestion.prompt}</span>
              <span className="mx-2 text-gray-400">=</span>
              <span>{currentQuestion.trueFalseAnswer}</span>
            </div>

            <div className="grid gap-3 sm:grid-cols-2">
              <Button
                type="button"
                variant="outline"
                className="border-gray-200 hover:border-pink-300 hover:bg-pink-50 hover:text-pink-600"
                disabled={isAnswered}
                onClick={() => handleTrueFalseAnswer(true)}
              >
                True
              </Button>

              <Button
                type="button"
                variant="outline"
                className="border-gray-200 hover:border-pink-300 hover:bg-pink-50 hover:text-pink-600"
                disabled={isAnswered}
                onClick={() => handleTrueFalseAnswer(false)}
              >
                False
              </Button>
            </div>

            {isAnswered && (
              <div
                className={`rounded-xl border p-4 text-sm ${
                  selectedAnswer ===
                  (currentQuestion.isTrueStatement ? "True" : "False")
                    ? "border-green-200 bg-green-50 text-green-700"
                    : "border-red-200 bg-red-50 text-red-700"
                }`}
              >
                Correct answer:{" "}
                <span className="font-semibold">
                  {currentQuestion.isTrueStatement ? "True" : "False"}
                </span>
              </div>
            )}
          </div>
        )}

        {currentQuestion.type === "written" && (
          <div className="space-y-3">
            <Input
              value={writtenAnswer}
              disabled={isAnswered}
              placeholder="Type your answer..."
              className="h-12 rounded-xl border-gray-200 focus-visible:ring-pink-400"
              onChange={(event) => setWrittenAnswer(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === "Enter") {
                  if (isAnswered) {
                    goToNextQuestion();
                  } else {
                    handleWrittenCheck();
                  }
                }
              }}
            />

            {isAnswered && (
              <div
                className={`rounded-xl border p-4 text-sm ${
                  normalizeAnswer(writtenAnswer) ===
                  normalizeAnswer(currentQuestion.correctAnswer)
                    ? "border-green-200 bg-green-50 text-green-700"
                    : "border-red-200 bg-red-50 text-red-700"
                }`}
              >
                Correct answer:{" "}
                <span className="font-semibold">
                  {currentQuestion.correctAnswer}
                </span>
              </div>
            )}
          </div>
        )}

        {currentQuestion.type === "matching" && (
          <div className="space-y-5">
            <div>
              <h2 className="text-xl font-bold text-gray-800">
                Match the pairs
              </h2>
              <p className="mt-1 text-sm text-gray-500">
                Matching is counted as correct only if you finish it without
                mistakes.
              </p>
            </div>

            <div className="grid min-w-0 gap-5 md:grid-cols-2">
              <div className="min-w-0 space-y-3">
                <h3 className="text-xs font-semibold uppercase tracking-wide text-gray-500">
                  Prompts
                </h3>

                {matchingPromptCards.map((card) => {
                  const isMatched = matchedIds.includes(card.flashcardId);
                  const isSelected = selectedPromptId === card.flashcardId;
                  const isMismatch = mismatchIds.includes(card.flashcardId);

                  return (
                    <Button
                      key={`prompt-${card.flashcardId}`}
                      type="button"
                      variant="outline"
                      disabled={isMatched || mismatchIds.length > 0}
                      className={`h-auto min-h-14 w-full min-w-0 justify-start whitespace-normal break-words rounded-xl px-4 py-3 text-left leading-relaxed disabled:opacity-100 ${
                        isMatched
                          ? "border-green-200 bg-green-50 text-green-700"
                          : isMismatch
                            ? "test-shake border-red-300 bg-red-50 text-red-700"
                            : isSelected
                              ? "border-pink-400 bg-pink-50 text-pink-600 ring-2 ring-pink-100"
                              : "border-gray-200 bg-white text-gray-700 hover:border-pink-300 hover:bg-pink-50 hover:text-pink-600"
                      }`}
                      onClick={() =>
                        handleMatchingPromptClick(card.flashcardId)
                      }
                    >
                      {getPromptValue(card, currentQuestion.answerWith)}
                    </Button>
                  );
                })}
              </div>

              <div className="min-w-0 space-y-3">
                <h3 className="text-xs font-semibold uppercase tracking-wide text-gray-500">
                  Answers
                </h3>

                {matchingAnswerCards.map((card) => {
                  const isMatched = matchedIds.includes(card.flashcardId);
                  const isSelected = selectedAnswerId === card.flashcardId;
                  const isMismatch = mismatchIds.includes(card.flashcardId);

                  return (
                    <Button
                      key={`answer-${card.flashcardId}`}
                      type="button"
                      variant="outline"
                      disabled={isMatched || mismatchIds.length > 0}
                      className={`h-auto min-h-14 w-full min-w-0 justify-start whitespace-normal break-words rounded-xl px-4 py-3 text-left leading-relaxed disabled:opacity-100 ${
                        isMatched
                          ? "border-green-200 bg-green-50 text-green-700"
                          : isMismatch
                            ? "test-shake border-red-300 bg-red-50 text-red-700"
                            : isSelected
                              ? "border-pink-400 bg-pink-50 text-pink-600 ring-2 ring-pink-100"
                              : "border-gray-200 bg-white text-gray-700 hover:border-pink-300 hover:bg-pink-50 hover:text-pink-600"
                      }`}
                      onClick={() =>
                        handleMatchingAnswerClick(card.flashcardId)
                      }
                    >
                      {getAnswerValue(card, currentQuestion.answerWith)}
                    </Button>
                  );
                })}
              </div>
            </div>

            <div className="text-sm text-gray-500">
              Mistakes:{" "}
              <span className="font-semibold text-pink-500">
                {matchingMistakes}
              </span>
            </div>
          </div>
        )}
      </div>

      <div className="flex justify-end">
        {currentQuestion.type === "written" && !isAnswered && (
          <Button
            type="button"
            className="bg-pink-500 text-white hover:bg-pink-600"
            disabled={!writtenAnswer.trim()}
            onClick={handleWrittenCheck}
          >
            Check answer
          </Button>
        )}

        {currentQuestion.type === "matching" && isMatchingCompleted() && (
          <Button
            type="button"
            className="bg-pink-500 text-white hover:bg-pink-600"
            onClick={finishMatchingQuestion}
          >
            Next question
          </Button>
        )}

        {currentQuestion.type !== "matching" && isAnswered && (
          <Button
            type="button"
            className="bg-pink-500 text-white hover:bg-pink-600"
            onClick={goToNextQuestion}
          >
            Next question
          </Button>
        )}
      </div>

      <style jsx global>{`
        .test-question-enter {
          animation: test-question-enter 280ms ease-out;
        }

        .test-shake {
          animation: test-shake 260ms ease-in-out;
        }

        @keyframes test-question-enter {
          from {
            opacity: 0;
            transform: translateY(12px) scale(0.98);
          }

          to {
            opacity: 1;
            transform: translateY(0) scale(1);
          }
        }

        @keyframes test-shake {
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
