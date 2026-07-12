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
import {
  createQuestionResult,
  mapCustomTestQuestionType,
} from "@/components/games/utils/gameQuestionResults";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import type { SaveGameQuestionResultRequest } from "@/types/games";

import { shuffleArray } from "./game-utils";

type AnswerWith = "term" | "definition" | "both";

type TestQuestionType = "trueFalse" | "multipleChoice" | "matching" | "written";

interface TestGameProps {
  flashcards: Flashcard[];
  setId: number | string;
}

interface TestConfig {
  questionCount: number;
  answerWith: AnswerWith;
  enabledTypes: Record<TestQuestionType, boolean>;
}

interface TestQuestion {
  id: string;
  type: TestQuestionType;
  flashcard: Flashcard;
  prompt: string;
  correctAnswer: string;
  options?: string[];
  trueFalseAnswer?: string;
  isTrueStatement?: boolean;
  matchingCards?: Flashcard[];
  matchingAnswerCards?: Flashcard[];
  answerWith: Exclude<AnswerWith, "both">;
}

interface TestGameState {
  config: TestConfig;
  questions: TestQuestion[];
  questionResults: SaveGameQuestionResultRequest[];
  hasStarted: boolean;
  currentIndex: number;
  score: number;
  selectedAnswer: string | null;
  writtenAnswer: string;
  isAnswered: boolean;
  selectedPromptId: number | null;
  selectedAnswerId: number | null;
  matchedIds: number[];
  matchingMistakes: number;
  matchingMistakesByFlashcardId: Record<number, number>;
  mismatchIds: number[];
  isResultSaved: boolean;
  startedAt: number | null;
  finishedAt: number | null;
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

function incrementMatchingMistakesByFlashcardId({
  mistakesByFlashcardId,
  promptId,
  answerId,
}: {
  mistakesByFlashcardId: Record<number, number>;
  promptId: number;
  answerId: number;
}) {
  return {
    ...mistakesByFlashcardId,
    [promptId]: (mistakesByFlashcardId[promptId] ?? 0) + 1,
    [answerId]: (mistakesByFlashcardId[answerId] ?? 0) + 1,
  };
}

function createInitialConfig(maxQuestions: number): TestConfig {
  return {
    questionCount: Math.min(20, Math.max(1, maxQuestions)),
    answerWith: "both",
    enabledTypes: {
      trueFalse: false,
      multipleChoice: true,
      matching: false,
      written: false,
    },
  };
}

function createInitialTestState(flashcards: Flashcard[]): TestGameState {
  return {
    config: createInitialConfig(flashcards.length),
    questions: [],
    questionResults: [],
    hasStarted: false,
    currentIndex: 0,
    score: 0,
    selectedAnswer: null,
    writtenAnswer: "",
    isAnswered: false,
    selectedPromptId: null,
    selectedAnswerId: null,
    matchedIds: [],
    matchingMistakes: 0,
    matchingMistakesByFlashcardId: {},
    mismatchIds: [],
    isResultSaved: false,
    startedAt: null,
    finishedAt: null,
  };
}

function getResetQuestionState() {
  return {
    selectedAnswer: null,
    writtenAnswer: "",
    isAnswered: false,
    selectedPromptId: null,
    selectedAnswerId: null,
    matchedIds: [],
    matchingMistakes: 0,
    matchingMistakesByFlashcardId: {},
    mismatchIds: [],
  };
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
        flashcard: card,
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
        flashcard: card,
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
        flashcard: card,
        prompt,
        correctAnswer,
        matchingCards,
        matchingAnswerCards: shuffleArray(matchingCards),
        answerWith,
      };
    }

    return {
      id: `${type}-${card.flashcardId}-${index}`,
      type,
      flashcard: card,
      prompt,
      correctAnswer,
      answerWith,
    };
  });
}

const defaultOptionClass =
  "h-auto min-h-12 justify-start whitespace-normal break-words border-border bg-background text-left text-foreground hover:border-pink-300 hover:bg-pink-50 hover:text-pink-600 dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-400";

const correctOptionClass =
  "h-auto min-h-12 justify-start whitespace-normal break-words border-green-300 bg-green-50 text-left text-green-700 dark:border-green-900 dark:bg-green-950/30 dark:text-green-400";

const incorrectOptionClass =
  "h-auto min-h-12 justify-start whitespace-normal break-words border-red-300 bg-red-50 text-left text-red-700 dark:border-red-900 dark:bg-red-950/30 dark:text-red-400";

const matchingDefaultClass =
  "border-border bg-background text-foreground hover:border-pink-300 hover:bg-pink-50 hover:text-pink-600 dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-400";

const matchingSelectedClass =
  "border-pink-400 bg-pink-50 text-pink-600 ring-2 ring-pink-100 dark:border-pink-700 dark:bg-pink-950/40 dark:text-pink-400 dark:ring-pink-900/50";

const matchingMismatchClass =
  "game-shake border-red-300 bg-red-50 text-red-700 dark:border-red-900 dark:bg-red-950/30 dark:text-red-400";

const matchingCorrectClass =
  "border-green-200 bg-green-50 text-green-700 dark:border-green-900 dark:bg-green-950/30 dark:text-green-400";

export default function TestGame({ flashcards, setId }: TestGameProps) {
  const maxQuestions = flashcards.length;

  const storageKey = getGameStorageKey(setId, "custom-test");

  const [gameState, setGameState, clearGameState] =
    usePersistedGameState<TestGameState>(storageKey, () =>
      createInitialTestState(flashcards),
    );

  const {
    config,
    questions,
    questionResults = [],
    hasStarted,
    currentIndex,
    score,
    selectedAnswer,
    writtenAnswer,
    isAnswered,
    selectedPromptId,
    selectedAnswerId,
    matchedIds,
    matchingMistakes,
    matchingMistakesByFlashcardId = {},
    mismatchIds,
    isResultSaved,
    startedAt,
    finishedAt,
  } = gameState;

  const currentQuestion = questions[currentIndex];

  const isFinished = hasStarted && currentIndex >= questions.length;

  const elapsedSeconds = useGameElapsedSeconds(startedAt, finishedAt);

  const durationSeconds = isFinished
    ? getElapsedGameSeconds(startedAt, finishedAt)
    : undefined;

  const enabledTypesCount = Object.values(config.enabledTypes).filter(
    Boolean,
  ).length;

  const matchingPromptCards = useMemo(
    () => currentQuestion?.matchingCards ?? [],
    [currentQuestion],
  );

  const matchingAnswerCards = useMemo(
    () => currentQuestion?.matchingAnswerCards ?? [],
    [currentQuestion],
  );

  function updateGameState(nextState: Partial<TestGameState>) {
    setGameState((previousState) => ({
      ...previousState,
      ...nextState,
    }));
  }

  useSaveGameResultOnFinish({
    setId,
    mode: "custom-test",
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

  function updateConfig(nextConfig: Partial<TestConfig>) {
    setGameState((previousState) => ({
      ...previousState,
      config: {
        ...previousState.config,
        ...nextConfig,
      },
    }));
  }

  function updateQuestionCount(value: string) {
    const numberValue = Number(value);

    if (Number.isNaN(numberValue)) {
      return;
    }

    updateConfig({
      questionCount: Math.min(Math.max(numberValue, 1), maxQuestions),
    });
  }

  function toggleQuestionType(type: TestQuestionType) {
    setGameState((previousState) => ({
      ...previousState,
      config: {
        ...previousState.config,
        enabledTypes: {
          ...previousState.config.enabledTypes,
          [type]: !previousState.config.enabledTypes[type],
        },
      },
    }));
  }

  function startTest() {
    if (enabledTypesCount === 0) {
      return;
    }

    const nextQuestions = buildTestQuestions(flashcards, config);

    updateGameState({
      questions: nextQuestions,
      questionResults: [],
      hasStarted: true,
      currentIndex: 0,
      score: 0,
      isResultSaved: false,
      startedAt: Date.now(),
      finishedAt: null,
      ...getResetQuestionState(),
    });
  }

  function goToNextQuestion() {
    const nextIndex = currentIndex + 1;

    updateGameState({
      currentIndex: nextIndex,
      finishedAt: nextIndex >= questions.length ? Date.now() : finishedAt,
      ...getResetQuestionState(),
    });
  }

  function restartSetup() {
    updateGameState({
      questions: [],
      questionResults: [],
      hasStarted: false,
      currentIndex: 0,
      score: 0,
      isResultSaved: false,
      startedAt: null,
      finishedAt: null,
      ...getResetQuestionState(),
    });
  }

  function resetEverything() {
    clearGameState();

    setGameState(createInitialTestState(flashcards));
  }

  function createCurrentQuestionResult({
    userAnswer,
    wasCorrect,
  }: {
    userAnswer: string;
    wasCorrect: boolean;
  }) {
    if (!currentQuestion || currentQuestion.type === "matching") {
      return null;
    }

    const questionType = mapCustomTestQuestionType(currentQuestion.type);

    const prompt =
      currentQuestion.type === "trueFalse"
        ? `${currentQuestion.prompt} = ${currentQuestion.trueFalseAnswer}`
        : currentQuestion.prompt;

    const correctAnswer =
      currentQuestion.type === "trueFalse"
        ? currentQuestion.isTrueStatement
          ? "True"
          : "False"
        : currentQuestion.correctAnswer;

    return createQuestionResult({
      flashcardId: currentQuestion.flashcard.flashcardId,
      questionKey: currentQuestion.id,
      questionOrder: currentIndex,
      questionType,
      answerWith: currentQuestion.answerWith,
      prompt,
      userAnswer,
      correctAnswer,
      wasCorrect,
    });
  }

  function createCurrentMatchingQuestionResults() {
    if (!currentQuestion?.matchingCards) {
      return [];
    }

    return currentQuestion.matchingCards.map((card, index) => {
      const mistakesCount =
        matchingMistakesByFlashcardId[card.flashcardId] ?? 0;

      return createQuestionResult({
        flashcardId: card.flashcardId,
        questionKey: `${currentQuestion.id}-${card.flashcardId}`,
        questionOrder: currentIndex + index,
        questionType: "matching",
        answerWith: currentQuestion.answerWith,
        prompt: getPromptValue(card, currentQuestion.answerWith),
        correctAnswer: getAnswerValue(card, currentQuestion.answerWith),
        wasCorrect: mistakesCount === 0,
        mistakesCount,
      });
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
      isAnswered: true,
      score: isCorrect ? score + 1 : score,
      questionResults: questionResult
        ? [...questionResults, questionResult]
        : questionResults,
    });
  }

  function handleTrueFalseAnswer(answer: boolean) {
    if (!currentQuestion || isAnswered) {
      return;
    }

    const isCorrect = answer === currentQuestion.isTrueStatement;

    const userAnswer = answer ? "True" : "False";

    const questionResult = createCurrentQuestionResult({
      userAnswer,
      wasCorrect: isCorrect,
    });

    updateGameState({
      selectedAnswer: userAnswer,
      isAnswered: true,
      score: isCorrect ? score + 1 : score,
      questionResults: questionResult
        ? [...questionResults, questionResult]
        : questionResults,
    });
  }

  function handleWrittenCheck() {
    if (!currentQuestion || isAnswered || !writtenAnswer.trim()) {
      return;
    }

    const isCorrect =
      normalizeAnswer(writtenAnswer) ===
      normalizeAnswer(currentQuestion.correctAnswer);

    const questionResult = createCurrentQuestionResult({
      userAnswer: writtenAnswer,
      wasCorrect: isCorrect,
    });

    updateGameState({
      isAnswered: true,
      score: isCorrect ? score + 1 : score,
      questionResults: questionResult
        ? [...questionResults, questionResult]
        : questionResults,
    });
  }

  function isMatchingCompleted() {
    return Boolean(
      currentQuestion?.type === "matching" &&
      currentQuestion.matchingCards &&
      matchedIds.length === currentQuestion.matchingCards.length,
    );
  }

  function handleMatchingPromptClick(cardId: number) {
    if (matchedIds.includes(cardId) || mismatchIds.length > 0) {
      return;
    }

    updateGameState({
      selectedPromptId: cardId,
    });

    if (selectedAnswerId !== null) {
      checkMatchingPair(cardId, selectedAnswerId);
    }
  }

  function handleMatchingAnswerClick(cardId: number) {
    if (matchedIds.includes(cardId) || mismatchIds.length > 0) {
      return;
    }

    updateGameState({
      selectedAnswerId: cardId,
    });

    if (selectedPromptId !== null) {
      checkMatchingPair(selectedPromptId, cardId);
    }
  }

  function checkMatchingPair(promptId: number, answerId: number) {
    if (promptId === answerId) {
      updateGameState({
        matchedIds: matchedIds.includes(promptId)
          ? matchedIds
          : [...matchedIds, promptId],
        selectedPromptId: null,
        selectedAnswerId: null,
      });

      return;
    }

    updateGameState({
      matchingMistakes: matchingMistakes + 1,
      matchingMistakesByFlashcardId: incrementMatchingMistakesByFlashcardId({
        mistakesByFlashcardId: matchingMistakesByFlashcardId,
        promptId,
        answerId,
      }),
      mismatchIds: [promptId, answerId],
    });

    window.setTimeout(() => {
      updateGameState({
        selectedPromptId: null,
        selectedAnswerId: null,
        mismatchIds: [],
      });
    }, 550);
  }

  function finishMatchingQuestion() {
    const nextIndex = currentIndex + 1;

    const matchingQuestionResults = createCurrentMatchingQuestionResults();

    updateGameState({
      score: matchingMistakes === 0 ? score + 1 : score,
      questionResults: [...questionResults, ...matchingQuestionResults],
      currentIndex: nextIndex,
      finishedAt: nextIndex >= questions.length ? Date.now() : finishedAt,
      ...getResetQuestionState(),
    });
  }

  if (flashcards.length < 2) {
    return (
      <GameEmptyState message="Add at least 2 flashcards to start custom test mode." />
    );
  }

  if (!hasStarted) {
    return (
      <GameShell>
        <div className="mb-8">
          <p className="text-sm font-semibold text-pink-500 dark:text-pink-400">
            Custom test
          </p>

          <h1 className="mt-1 text-3xl font-bold text-foreground">
            Set up your test
          </h1>

          <p className="mt-2 text-sm text-muted-foreground">
            Choose question types and how answers should be shown.
          </p>
        </div>

        <div className="space-y-6">
          <div className="flex items-center justify-between gap-4">
            <label
              htmlFor="test-question-count"
              className="font-semibold text-foreground"
            >
              Questions{" "}
              <span className="text-sm font-normal text-muted-foreground">
                (max {maxQuestions})
              </span>
            </label>

            <Input
              id="test-question-count"
              type="number"
              min={1}
              max={maxQuestions}
              value={config.questionCount}
              className="h-12 w-24 rounded-xl border-input bg-background text-center font-semibold text-foreground focus-visible:border-pink-300 focus-visible:ring-pink-500/20"
              onChange={(event) => updateQuestionCount(event.target.value)}
            />
          </div>

          <div className="flex items-center justify-between gap-4 border-b border-border pb-5">
            <label
              htmlFor="test-answer-with"
              className="font-semibold text-foreground"
            >
              Answer with
            </label>

            <select
              id="test-answer-with"
              value={config.answerWith}
              className="h-11 rounded-full border border-input bg-background px-4 text-sm font-semibold text-foreground outline-none transition hover:border-pink-200 focus:border-pink-400 focus:ring-4 focus:ring-pink-500/15 dark:hover:border-pink-900"
              onChange={(event) =>
                updateConfig({
                  answerWith: event.target.value as AnswerWith,
                })
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
                <span className="font-semibold text-foreground">{label}</span>

                <button
                  type="button"
                  role="switch"
                  aria-checked={isEnabled}
                  aria-label={`Enable ${label}`}
                  className={`relative h-7 w-12 rounded-full transition-colors ${
                    isEnabled ? "bg-pink-500" : "bg-muted"
                  }`}
                  onClick={() => toggleQuestionType(questionType)}
                >
                  <span
                    className={`absolute top-1 h-5 w-5 rounded-full bg-white shadow-sm transition-all ${
                      isEnabled ? "left-6" : "left-1"
                    }`}
                  />
                </button>
              </div>
            );
          })}
        </div>

        {enabledTypesCount === 0 && (
          <p
            className="mt-6 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700 dark:border-red-900 dark:bg-red-950/30 dark:text-red-400"
            role="alert"
          >
            Select at least one question type.
          </p>
        )}

        <div className="mt-8 flex justify-between gap-3">
          <Button
            type="button"
            variant="outline"
            className="border-border bg-background text-pink-500 hover:border-pink-200 hover:bg-pink-50 dark:text-pink-400 dark:hover:border-pink-900 dark:hover:bg-pink-950/30"
            onClick={resetEverything}
          >
            Reset
          </Button>

          <Button
            type="button"
            className="rounded-full bg-pink-500 px-6 text-white hover:bg-pink-600"
            disabled={enabledTypesCount === 0}
            onClick={startTest}
          >
            Start test
          </Button>
        </div>
      </GameShell>
    );
  }

  if (isFinished) {
    return (
      <GameResultCard
        title="Custom test completed"
        scoreLabel="Your score"
        score={score}
        total={questions.length}
        progressSuffix="correct"
        primaryActionLabel="Try again"
        onPrimaryAction={startTest}
        secondaryActionLabel="Change settings"
        onSecondaryAction={restartSetup}
      >
        <div className="mt-4 flex justify-center">
          <GameTimer seconds={elapsedSeconds} />
        </div>
      </GameResultCard>
    );
  }

  if (!currentQuestion) {
    return null;
  }

  return (
    <GameShell maxWidth="lg">
      <GameProgress
        current={currentIndex}
        total={questions.length}
        suffix="answered"
      />

      <div className="flex justify-end">
        <GameTimer seconds={elapsedSeconds} />
      </div>

      <div key={currentQuestion.id} className="game-enter space-y-6">
        <div className="rounded-2xl border border-pink-200 bg-pink-50/60 p-5 dark:border-pink-900 dark:bg-pink-950/20">
          <div className="flex items-center justify-between gap-4 text-sm text-muted-foreground">
            <span>
              Question {currentIndex + 1} of {questions.length}
            </span>

            <span className="font-medium capitalize">
              {currentQuestion.type.replace(/([A-Z])/g, " $1")}
            </span>
          </div>

          {currentQuestion.type !== "matching" && (
            <>
              <p className="mt-5 text-sm text-muted-foreground">
                Answer with {currentQuestion.answerWith}
              </p>

              <h1 className="mt-2 break-words text-2xl font-bold text-foreground">
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

              let className = defaultOptionClass;

              if (isAnswered && isCorrect) {
                className = correctOptionClass;
              }

              if (isAnswered && isSelected && !isCorrect) {
                className = incorrectOptionClass;
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
            <div className="rounded-xl border border-border bg-background p-4 text-foreground">
              <span className="font-semibold">{currentQuestion.prompt}</span>

              <span className="mx-2 text-muted-foreground">=</span>

              <span>{currentQuestion.trueFalseAnswer}</span>
            </div>

            <div className="grid gap-3 sm:grid-cols-2">
              <Button
                type="button"
                variant="outline"
                className="border-border bg-background text-foreground hover:border-pink-300 hover:bg-pink-50 hover:text-pink-600 dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-400"
                disabled={isAnswered}
                onClick={() => handleTrueFalseAnswer(true)}
              >
                True
              </Button>

              <Button
                type="button"
                variant="outline"
                className="border-border bg-background text-foreground hover:border-pink-300 hover:bg-pink-50 hover:text-pink-600 dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-400"
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
                    ? "border-green-200 bg-green-50 text-green-700 dark:border-green-900 dark:bg-green-950/30 dark:text-green-400"
                    : "border-red-200 bg-red-50 text-red-700 dark:border-red-900 dark:bg-red-950/30 dark:text-red-400"
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
              className="h-12 rounded-xl border-input bg-background text-foreground placeholder:text-muted-foreground focus-visible:border-pink-300 focus-visible:ring-pink-500/20 dark:focus-visible:border-pink-800"
              onChange={(event) =>
                updateGameState({
                  writtenAnswer: event.target.value,
                })
              }
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
                    ? "border-green-200 bg-green-50 text-green-700 dark:border-green-900 dark:bg-green-950/30 dark:text-green-400"
                    : "border-red-200 bg-red-50 text-red-700 dark:border-red-900 dark:bg-red-950/30 dark:text-red-400"
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
              <h2 className="text-xl font-bold text-foreground">
                Match the pairs
              </h2>

              <p className="mt-1 text-sm text-muted-foreground">
                Matching is counted as correct only if you finish it without
                mistakes.
              </p>
            </div>

            <div className="grid min-w-0 gap-5 md:grid-cols-2">
              <div className="min-w-0 space-y-3">
                <h3 className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Prompts
                </h3>

                {matchingPromptCards.map((card) => {
                  const isMatched = matchedIds.includes(card.flashcardId);

                  const isSelected = selectedPromptId === card.flashcardId;

                  const isMismatch = mismatchIds.includes(card.flashcardId);

                  const stateClassName = isMatched
                    ? matchingCorrectClass
                    : isMismatch
                      ? matchingMismatchClass
                      : isSelected
                        ? matchingSelectedClass
                        : matchingDefaultClass;

                  return (
                    <Button
                      key={`prompt-${card.flashcardId}`}
                      type="button"
                      variant="outline"
                      disabled={isMatched || mismatchIds.length > 0}
                      className={`h-auto min-h-14 w-full min-w-0 justify-start whitespace-normal break-words rounded-xl px-4 py-3 text-left leading-relaxed disabled:opacity-100 ${stateClassName}`}
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
                <h3 className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Answers
                </h3>

                {matchingAnswerCards.map((card) => {
                  const isMatched = matchedIds.includes(card.flashcardId);

                  const isSelected = selectedAnswerId === card.flashcardId;

                  const isMismatch = mismatchIds.includes(card.flashcardId);

                  const stateClassName = isMatched
                    ? matchingCorrectClass
                    : isMismatch
                      ? matchingMismatchClass
                      : isSelected
                        ? matchingSelectedClass
                        : matchingDefaultClass;

                  return (
                    <Button
                      key={`answer-${card.flashcardId}`}
                      type="button"
                      variant="outline"
                      disabled={isMatched || mismatchIds.length > 0}
                      className={`h-auto min-h-14 w-full min-w-0 justify-start whitespace-normal break-words rounded-xl px-4 py-3 text-left leading-relaxed disabled:opacity-100 ${stateClassName}`}
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

            <div className="text-sm text-muted-foreground">
              Mistakes:{" "}
              <span className="font-semibold text-pink-500 dark:text-pink-400">
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
    </GameShell>
  );
}
