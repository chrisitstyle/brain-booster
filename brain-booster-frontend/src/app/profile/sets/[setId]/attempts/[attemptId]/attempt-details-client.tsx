"use client";

import Link from "next/link";
import type { ReactNode } from "react";
import {
  ArrowLeft,
  BarChart3,
  BookOpen,
  CheckCircle2,
  ClipboardList,
  Clock3,
  HelpCircle,
  Layers3,
  MessageCircle,
  Target,
  Timer,
  Trophy,
  XCircle,
} from "lucide-react";

import { useAuth } from "@/context/AuthContext";
import { useGameAttempt, useGameQuestionResults } from "@/hooks/game-results";
import type { GameQuestionResult } from "@/types/games";

interface AttemptDetailsClientProps {
  setId: number;
  attemptId: number;
}

function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return "No date";
  }

  return new Intl.DateTimeFormat("en-US", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

function formatDuration(seconds: number | null | undefined) {
  if (!seconds) {
    return "0:00";
  }

  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;

  return `${minutes}:${String(remainingSeconds).padStart(2, "0")}`;
}

function formatPercentage(value: number | null | undefined) {
  return `${Math.round(value ?? 0)}%`;
}

function formatLabel(value: string | null | undefined) {
  if (!value) {
    return "Unknown";
  }

  return value
    .split("-")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function getAccuracy(score: number, totalQuestions: number) {
  if (totalQuestions === 0) {
    return 0;
  }

  return (score / totalQuestions) * 100;
}

function StatusMessage({
  type = "default",
  children,
}: {
  type?: "default" | "error";
  children: ReactNode;
}) {
  return (
    <p
      className={
        type === "error"
          ? "rounded-xl border border-pink-200 bg-pink-50 px-4 py-3 text-sm text-pink-600"
          : "rounded-xl border border-dashed border-gray-200 bg-gray-50 px-4 py-3 text-sm text-slate-500"
      }
    >
      {children}
    </p>
  );
}

function IconBox({
  children,
  variant = "default",
}: {
  children: ReactNode;
  variant?: "default" | "gold" | "green" | "red";
}) {
  const variantClassName = {
    default: "bg-pink-100 text-pink-500",
    gold: "bg-amber-100 text-amber-500",
    green: "bg-green-50 text-green-600",
    red: "bg-red-50 text-red-600",
  }[variant];

  return (
    <div
      className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl ${variantClassName}`}
    >
      {children}
    </div>
  );
}

function MetricCard({
  icon,
  iconVariant = "default",
  label,
  value,
}: {
  icon: ReactNode;
  iconVariant?: "default" | "gold" | "green" | "red";
  label: string;
  value: ReactNode;
}) {
  return (
    <div className="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm transition hover:border-pink-200 hover:shadow-md">
      <div className="h-1 bg-pink-500" />

      <div className="flex items-start justify-between gap-3 p-5">
        <div>
          <p className="text-sm font-medium text-slate-500">{label}</p>

          <p className="mt-3 text-2xl font-bold tracking-tight text-slate-950">
            {value}
          </p>
        </div>

        <IconBox variant={iconVariant}>{icon}</IconBox>
      </div>
    </div>
  );
}

function ResultBadge({ wasCorrect }: { wasCorrect: boolean }) {
  if (wasCorrect) {
    return (
      <span className="inline-flex items-center gap-1 rounded-full bg-green-50 px-3 py-1 text-xs font-semibold text-green-600">
        <CheckCircle2 size={14} />
        Correct
      </span>
    );
  }

  return (
    <span className="inline-flex items-center gap-1 rounded-full bg-red-50 px-3 py-1 text-xs font-semibold text-red-600">
      <XCircle size={14} />
      Incorrect
    </span>
  );
}

function AnswerBlock({
  label,
  value,
  variant = "default",
}: {
  label: string;
  value: string | null | undefined;
  variant?: "default" | "correct" | "incorrect";
}) {
  const labelClassName =
    variant === "correct"
      ? "text-green-600"
      : variant === "incorrect"
        ? "text-red-600"
        : "text-slate-500";

  return (
    <div className="rounded-2xl bg-gray-50 p-4">
      <p className={`text-xs font-semibold ${labelClassName}`}>{label}</p>

      <p className="mt-2 text-sm text-slate-950">{value || "No answer"}</p>
    </div>
  );
}

function QuestionResultCard({
  questionResult,
}: {
  questionResult: GameQuestionResult;
}) {
  return (
    <article className="overflow-hidden rounded-3xl border border-gray-200 bg-white shadow-sm">
      <div
        className={
          questionResult.wasCorrect ? "h-1 bg-green-500" : "h-1 bg-red-500"
        }
      />

      <div className="space-y-5 p-5">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <span className="rounded-full bg-gray-100 px-3 py-1 text-xs font-semibold text-slate-600">
                Question #{questionResult.questionOrder}
              </span>

              <span className="rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500">
                {formatLabel(questionResult.questionType)}
              </span>

              {questionResult.answerWith ? (
                <span className="rounded-full bg-gray-100 px-3 py-1 text-xs font-semibold text-slate-600">
                  Answer with: {formatLabel(questionResult.answerWith)}
                </span>
              ) : null}
            </div>

            <h3 className="mt-3 text-lg font-semibold text-slate-950">
              Flashcard #{questionResult.flashcardId}
            </h3>
          </div>

          <ResultBadge wasCorrect={questionResult.wasCorrect} />
        </div>

        {questionResult.prompt ? (
          <div className="rounded-2xl border border-gray-200 bg-white p-4">
            <div className="flex items-center gap-2 text-sm font-semibold text-slate-700">
              <HelpCircle size={16} className="text-pink-500" />
              Prompt
            </div>

            <p className="mt-2 text-sm text-slate-600">
              {questionResult.prompt}
            </p>
          </div>
        ) : null}

        <div className="grid gap-3 md:grid-cols-2">
          <AnswerBlock
            label="Your answer"
            value={questionResult.userAnswer}
            variant={questionResult.wasCorrect ? "correct" : "incorrect"}
          />

          <AnswerBlock
            label="Correct answer"
            value={questionResult.correctAnswer}
            variant="correct"
          />
        </div>

        <div className="grid gap-3 text-sm sm:grid-cols-3">
          <div className="rounded-2xl bg-gray-50 p-3">
            <div className="flex items-center gap-2 text-slate-500">
              <MessageCircle size={15} />
              <span>Question key</span>
            </div>

            <p className="mt-1 font-semibold text-slate-950">
              {questionResult.questionKey || "No key"}
            </p>
          </div>

          <div className="rounded-2xl bg-gray-50 p-3">
            <div className="flex items-center gap-2 text-slate-500">
              <XCircle size={15} />
              <span>Mistakes</span>
            </div>

            <p className="mt-1 font-semibold text-slate-950">
              {questionResult.mistakesCount}
            </p>
          </div>

          <div className="rounded-2xl bg-gray-50 p-3">
            <div className="flex items-center gap-2 text-slate-500">
              <Clock3 size={15} />
              <span>Answered at</span>
            </div>

            <p className="mt-1 font-semibold text-slate-950">
              {formatDateTime(questionResult.answeredAt)}
            </p>
          </div>
        </div>
      </div>
    </article>
  );
}

export default function AttemptDetailsClient({
  setId,
  attemptId,
}: AttemptDetailsClientProps) {
  const { token, isAuthenticated, isAuthLoading } = useAuth();

  const {
    data: attempt,
    isLoading: isAttemptLoading,
    error: attemptError,
  } = useGameAttempt(attemptId, token);

  const {
    data: questionResults,
    isLoading: areQuestionResultsLoading,
    error: questionResultsError,
  } = useGameQuestionResults(attemptId, token);

  const accuracy = attempt
    ? getAccuracy(attempt.score, attempt.totalQuestions)
    : 0;

  const attemptsHref = `/profile/sets/${setId}/attempts`;
  const statsHref = `/profile/sets/${setId}/stats`;

  const hasAttemptSetMismatch = Boolean(attempt && attempt.setId !== setId);

  const canDisplayAttempt = Boolean(attempt) && !hasAttemptSetMismatch;

  const canDisplayQuestionResults =
    !isAttemptLoading && !attemptError && canDisplayAttempt;

  if (isAuthLoading) {
    return (
      <main className="min-h-[calc(100vh-4rem)] bg-gray-50">
        <div className="mx-auto max-w-6xl px-4 py-10">
          <StatusMessage>Loading attempt details...</StatusMessage>
        </div>
      </main>
    );
  }

  if (!token || !isAuthenticated) {
    return (
      <main className="min-h-[calc(100vh-4rem)] bg-gray-50">
        <div className="mx-auto max-w-6xl px-4 py-10">
          <StatusMessage type="error">
            You need to be logged in to view attempt details.
          </StatusMessage>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-[calc(100vh-4rem)] bg-gray-50">
      <div className="mx-auto max-w-6xl space-y-8 px-4 py-8">
        <div className="flex flex-wrap items-center gap-4">
          <Link
            href={attemptsHref}
            className="inline-flex items-center gap-2 text-sm font-medium text-slate-500 transition hover:text-pink-500"
          >
            <ArrowLeft size={16} />
            Back to attempts
          </Link>

          <Link
            href={statsHref}
            className="text-sm font-medium text-pink-500 transition hover:text-pink-600"
          >
            View set statistics
          </Link>
        </div>

        <section className="overflow-hidden rounded-3xl border border-gray-200 bg-white shadow-sm">
          <div className="bg-pink-50/70 p-6 sm:p-8">
            <p className="mb-3 inline-flex items-center gap-2 rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500">
              <BarChart3 size={14} />
              Attempt details
            </p>

            <h1 className="text-3xl font-bold tracking-tight text-slate-950 sm:text-4xl">
              Attempt #{attemptId}
            </h1>

            <p className="mt-3 flex max-w-2xl items-start gap-2 text-slate-500">
              <BookOpen size={18} className="mt-0.5 shrink-0 text-pink-500" />

              <span>
                Review score, duration, mode, and every question from this
                completed attempt.
              </span>
            </p>
          </div>
        </section>

        {isAttemptLoading ? (
          <StatusMessage>Loading attempt summary...</StatusMessage>
        ) : null}

        {attemptError ? (
          <StatusMessage type="error">{attemptError}</StatusMessage>
        ) : null}

        {!isAttemptLoading && !attemptError && hasAttemptSetMismatch ? (
          <StatusMessage type="error">
            This attempt does not belong to the flashcard set from the current
            URL.
          </StatusMessage>
        ) : null}

        {canDisplayAttempt && attempt ? (
          <>
            <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <MetricCard
                icon={<Target size={20} />}
                label="Score"
                value={`${attempt.score}/${attempt.totalQuestions}`}
              />

              <MetricCard
                icon={<BarChart3 size={20} />}
                label="Accuracy"
                value={formatPercentage(accuracy)}
              />

              <MetricCard
                icon={<Trophy size={20} />}
                iconVariant="gold"
                label="Mode"
                value={formatLabel(attempt.mode)}
              />

              <MetricCard
                icon={<Timer size={20} />}
                label="Duration"
                value={formatDuration(attempt.durationSeconds)}
              />
            </section>

            <section className="rounded-3xl border border-gray-200 bg-white p-5 shadow-sm">
              <div className="grid gap-4 md:grid-cols-3">
                <div>
                  <p className="flex items-center gap-2 text-sm font-medium text-slate-500">
                    <Layers3 size={16} />
                    Set ID
                  </p>

                  <p className="mt-1 font-semibold text-slate-950">
                    #{attempt.setId}
                  </p>
                </div>

                <div>
                  <p className="flex items-center gap-2 text-sm font-medium text-slate-500">
                    <Clock3 size={16} />
                    Completed at
                  </p>

                  <p className="mt-1 font-semibold text-slate-950">
                    {formatDateTime(attempt.completedAt)}
                  </p>
                </div>

                <div>
                  <p className="flex items-center gap-2 text-sm font-medium text-slate-500">
                    <BookOpen size={16} />
                    User ID
                  </p>

                  <p className="mt-1 font-semibold text-slate-950">
                    #{attempt.userId}
                  </p>
                </div>
              </div>
            </section>
          </>
        ) : null}

        {canDisplayQuestionResults ? (
          <section className="space-y-4">
            <div className="flex items-start gap-3">
              <IconBox>
                <ClipboardList size={20} />
              </IconBox>

              <div>
                <h2 className="text-lg font-semibold tracking-tight text-slate-950">
                  Question results
                </h2>

                <p className="mt-1 text-sm text-slate-500">
                  Detailed result for each question in this attempt.
                </p>
              </div>
            </div>

            {areQuestionResultsLoading ? (
              <StatusMessage>Loading question results...</StatusMessage>
            ) : null}

            {questionResultsError ? (
              <StatusMessage type="error">{questionResultsError}</StatusMessage>
            ) : null}

            {!areQuestionResultsLoading &&
            !questionResultsError &&
            questionResults &&
            questionResults.length > 0 ? (
              <div className="space-y-4">
                {questionResults.map((questionResult) => (
                  <QuestionResultCard
                    key={questionResult.questionResultId}
                    questionResult={questionResult}
                  />
                ))}
              </div>
            ) : null}

            {!areQuestionResultsLoading &&
            !questionResultsError &&
            questionResults &&
            questionResults.length === 0 ? (
              <StatusMessage>
                No question results found for this attempt.
              </StatusMessage>
            ) : null}
          </section>
        ) : null}
      </div>
    </main>
  );
}
