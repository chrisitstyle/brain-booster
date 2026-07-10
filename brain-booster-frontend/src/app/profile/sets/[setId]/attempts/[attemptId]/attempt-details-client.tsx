"use client";

import type { ReactNode } from "react";
import Link from "next/link";
import {
  AlertCircle,
  ArrowLeft,
  BarChart3,
  BookOpen,
  CheckCircle2,
  ClipboardList,
  Clock3,
  HelpCircle,
  Layers3,
  Loader2,
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

type IconVariant = "default" | "gold" | "green" | "red";

function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return "No date";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "Unknown date";
  }

  return new Intl.DateTimeFormat("en-US", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

function formatDuration(seconds: number | null | undefined) {
  const normalizedSeconds =
    typeof seconds === "number" && Number.isFinite(seconds)
      ? Math.max(0, Math.round(seconds))
      : 0;

  const minutes = Math.floor(normalizedSeconds / 60);

  const remainingSeconds = normalizedSeconds % 60;

  return `${minutes}:${String(remainingSeconds).padStart(2, "0")}`;
}

function clampPercentage(value: number | null | undefined) {
  const normalizedValue =
    typeof value === "number" && Number.isFinite(value) ? value : 0;

  return Math.min(Math.max(normalizedValue, 0), 100);
}

function formatPercentage(value: number | null | undefined) {
  return `${Math.round(clampPercentage(value))}%`;
}

function formatLabel(value: string | null | undefined) {
  if (!value) {
    return "Unknown";
  }

  return value
    .split(/[-_]/)
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1).toLowerCase())
    .join(" ");
}

function getAccuracy(score: number, totalQuestions: number) {
  if (
    !Number.isFinite(score) ||
    !Number.isFinite(totalQuestions) ||
    totalQuestions <= 0
  ) {
    return 0;
  }

  return clampPercentage((score / totalQuestions) * 100);
}

function StatusMessage({
  type = "default",
  children,
}: {
  type?: "default" | "error" | "loading";
  children: ReactNode;
}) {
  if (type === "error") {
    return (
      <div
        className="flex items-start gap-3 rounded-xl border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive"
        role="alert"
      >
        <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />

        <span>{children}</span>
      </div>
    );
  }

  return (
    <div
      className="flex items-center gap-3 rounded-xl border border-dashed border-border bg-card px-4 py-3 text-sm text-muted-foreground"
      role={type === "loading" ? "status" : undefined}
    >
      {type === "loading" && (
        <Loader2
          className="h-4 w-4 shrink-0 animate-spin text-pink-500 dark:text-pink-400"
          aria-hidden="true"
        />
      )}

      <span>{children}</span>
    </div>
  );
}

function IconBox({
  children,
  variant = "default",
}: {
  children: ReactNode;
  variant?: IconVariant;
}) {
  const variantClassName: Record<IconVariant, string> = {
    default: "bg-pink-100 text-pink-500 dark:bg-pink-950/50 dark:text-pink-400",
    gold: "bg-amber-100 text-amber-600 dark:bg-amber-950/50 dark:text-amber-400",
    green:
      "bg-green-100 text-green-600 dark:bg-green-950/50 dark:text-green-400",
    red: "bg-red-100 text-red-600 dark:bg-red-950/50 dark:text-red-400",
  };

  return (
    <div
      className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl ${variantClassName[variant]}`}
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
  iconVariant?: IconVariant;
  label: string;
  value: ReactNode;
}) {
  return (
    <div className="overflow-hidden rounded-2xl border border-border bg-card text-card-foreground shadow-sm transition-all hover:border-pink-200 hover:shadow-md dark:hover:border-pink-900">
      <div className="h-1 bg-pink-500" />

      <div className="flex items-start justify-between gap-3 p-5">
        <div className="min-w-0">
          <p className="text-sm font-medium text-muted-foreground">{label}</p>

          <p className="mt-3 break-words text-2xl font-bold tracking-tight text-card-foreground">
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
      <span className="inline-flex shrink-0 items-center gap-1 rounded-full bg-green-100 px-3 py-1 text-xs font-semibold text-green-700 dark:bg-green-950/50 dark:text-green-400">
        <CheckCircle2 size={14} aria-hidden="true" />
        Correct
      </span>
    );
  }

  return (
    <span className="inline-flex shrink-0 items-center gap-1 rounded-full bg-red-100 px-3 py-1 text-xs font-semibold text-red-700 dark:bg-red-950/50 dark:text-red-400">
      <XCircle size={14} aria-hidden="true" />
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
      ? "text-green-700 dark:text-green-400"
      : variant === "incorrect"
        ? "text-red-700 dark:text-red-400"
        : "text-muted-foreground";

  return (
    <div className="rounded-2xl bg-muted/60 p-4">
      <p className={`text-xs font-semibold ${labelClassName}`}>{label}</p>

      <p className="mt-2 whitespace-pre-wrap break-words text-sm text-foreground">
        {value?.trim() || "No answer"}
      </p>
    </div>
  );
}

function QuestionMetadataItem({
  icon,
  label,
  value,
}: {
  icon: ReactNode;
  label: string;
  value: ReactNode;
}) {
  return (
    <div className="rounded-2xl bg-muted/60 p-3">
      <div className="flex items-center gap-2 text-muted-foreground">
        {icon}
        <span>{label}</span>
      </div>

      <p className="mt-1 break-words font-semibold text-foreground">{value}</p>
    </div>
  );
}

function QuestionResultCard({
  questionResult,
}: {
  questionResult: GameQuestionResult;
}) {
  return (
    <article className="overflow-hidden rounded-3xl border border-border bg-card text-card-foreground shadow-sm">
      <div
        className={
          questionResult.wasCorrect ? "h-1 bg-green-500" : "h-1 bg-red-500"
        }
      />

      <div className="space-y-5 p-5">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <span className="rounded-full bg-muted px-3 py-1 text-xs font-semibold text-muted-foreground">
                Question #{questionResult.questionOrder}
              </span>

              <span className="rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
                {formatLabel(questionResult.questionType)}
              </span>

              {questionResult.answerWith && (
                <span className="rounded-full bg-muted px-3 py-1 text-xs font-semibold text-muted-foreground">
                  Answer with: {formatLabel(questionResult.answerWith)}
                </span>
              )}
            </div>

            <h3 className="mt-3 break-words text-lg font-semibold text-card-foreground">
              Flashcard #{questionResult.flashcardId}
            </h3>
          </div>

          <ResultBadge wasCorrect={questionResult.wasCorrect} />
        </div>

        {questionResult.prompt && (
          <div className="rounded-2xl border border-border bg-background p-4">
            <div className="flex items-center gap-2 text-sm font-semibold text-foreground">
              <HelpCircle
                size={16}
                className="text-pink-500 dark:text-pink-400"
                aria-hidden="true"
              />
              Prompt
            </div>

            <p className="mt-2 whitespace-pre-wrap break-words text-sm text-muted-foreground">
              {questionResult.prompt}
            </p>
          </div>
        )}

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
          <QuestionMetadataItem
            icon={<MessageCircle size={15} aria-hidden="true" />}
            label="Question key"
            value={questionResult.questionKey || "No key"}
          />

          <QuestionMetadataItem
            icon={<XCircle size={15} aria-hidden="true" />}
            label="Mistakes"
            value={questionResult.mistakesCount}
          />

          <QuestionMetadataItem
            icon={<Clock3 size={15} aria-hidden="true" />}
            label="Answered at"
            value={formatDateTime(questionResult.answeredAt)}
          />
        </div>
      </div>
    </article>
  );
}

function AttemptMetadataItem({
  icon,
  label,
  value,
}: {
  icon: ReactNode;
  label: string;
  value: ReactNode;
}) {
  return (
    <div>
      <p className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
        {icon}
        {label}
      </p>

      <p className="mt-1 break-words font-semibold text-card-foreground">
        {value}
      </p>
    </div>
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
      <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
        <div className="mx-auto max-w-6xl px-4 py-10">
          <StatusMessage type="loading">
            Loading attempt details...
          </StatusMessage>
        </div>
      </main>
    );
  }

  if (!token || !isAuthenticated) {
    return (
      <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
        <div className="mx-auto max-w-6xl px-4 py-10">
          <StatusMessage type="error">
            You need to be logged in to view attempt details.
          </StatusMessage>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
      <div className="mx-auto max-w-6xl space-y-8 px-4 py-8">
        <nav
          className="flex flex-wrap items-center gap-4"
          aria-label="Attempt navigation"
        >
          <Link
            href={attemptsHref}
            className="inline-flex items-center gap-2 rounded-sm text-sm font-medium text-muted-foreground transition-colors hover:text-pink-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring dark:hover:text-pink-400"
          >
            <ArrowLeft size={16} aria-hidden="true" />
            Back to attempts
          </Link>

          <Link
            href={statsHref}
            className="rounded-sm text-sm font-medium text-pink-500 transition-colors hover:text-pink-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring dark:text-pink-400 dark:hover:text-pink-300"
          >
            View set statistics
          </Link>
        </nav>

        <section className="overflow-hidden rounded-3xl border border-border bg-card text-card-foreground shadow-sm">
          <div className="bg-pink-50/70 p-6 dark:bg-pink-950/20 sm:p-8">
            <p className="mb-3 inline-flex items-center gap-2 rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
              <BarChart3 size={14} aria-hidden="true" />
              Attempt details
            </p>

            <h1 className="text-3xl font-bold tracking-tight text-card-foreground sm:text-4xl">
              Attempt #{attemptId}
            </h1>

            <p className="mt-3 flex max-w-2xl items-start gap-2 text-muted-foreground">
              <BookOpen
                size={18}
                className="mt-0.5 shrink-0 text-pink-500 dark:text-pink-400"
                aria-hidden="true"
              />

              <span>
                Review score, duration, mode, and every question from this
                completed attempt.
              </span>
            </p>
          </div>
        </section>

        {isAttemptLoading && (
          <StatusMessage type="loading">
            Loading attempt summary...
          </StatusMessage>
        )}

        {attemptError && (
          <StatusMessage type="error">{attemptError}</StatusMessage>
        )}

        {!isAttemptLoading && !attemptError && hasAttemptSetMismatch && (
          <StatusMessage type="error">
            This attempt does not belong to the flashcard set from the current
            URL.
          </StatusMessage>
        )}

        {canDisplayAttempt && attempt && (
          <>
            <section
              className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4"
              aria-label="Attempt summary"
            >
              <MetricCard
                icon={<Target size={20} aria-hidden="true" />}
                label="Score"
                value={`${attempt.score}/${attempt.totalQuestions}`}
              />

              <MetricCard
                icon={<BarChart3 size={20} aria-hidden="true" />}
                label="Accuracy"
                value={formatPercentage(accuracy)}
              />

              <MetricCard
                icon={<Trophy size={20} aria-hidden="true" />}
                iconVariant="gold"
                label="Mode"
                value={formatLabel(attempt.mode)}
              />

              <MetricCard
                icon={<Timer size={20} aria-hidden="true" />}
                label="Duration"
                value={formatDuration(attempt.durationSeconds)}
              />
            </section>

            <section className="rounded-3xl border border-border bg-card p-5 text-card-foreground shadow-sm">
              <div className="grid gap-4 md:grid-cols-3">
                <AttemptMetadataItem
                  icon={<Layers3 size={16} aria-hidden="true" />}
                  label="Set ID"
                  value={`#${attempt.setId}`}
                />

                <AttemptMetadataItem
                  icon={<Clock3 size={16} aria-hidden="true" />}
                  label="Completed at"
                  value={formatDateTime(attempt.completedAt)}
                />

                <AttemptMetadataItem
                  icon={<BookOpen size={16} aria-hidden="true" />}
                  label="User ID"
                  value={`#${attempt.userId}`}
                />
              </div>
            </section>
          </>
        )}

        {canDisplayQuestionResults && (
          <section
            className="space-y-4"
            aria-labelledby="question-results-heading"
          >
            <div className="flex items-start gap-3">
              <IconBox>
                <ClipboardList size={20} aria-hidden="true" />
              </IconBox>

              <div>
                <h2
                  id="question-results-heading"
                  className="text-lg font-semibold tracking-tight text-foreground"
                >
                  Question results
                </h2>

                <p className="mt-1 text-sm text-muted-foreground">
                  Detailed result for each question in this attempt.
                </p>
              </div>
            </div>

            {areQuestionResultsLoading && (
              <StatusMessage type="loading">
                Loading question results...
              </StatusMessage>
            )}

            {questionResultsError && (
              <StatusMessage type="error">{questionResultsError}</StatusMessage>
            )}

            {!areQuestionResultsLoading &&
              !questionResultsError &&
              questionResults &&
              questionResults.length > 0 && (
                <div className="space-y-4">
                  {questionResults.map((questionResult) => (
                    <QuestionResultCard
                      key={questionResult.questionResultId}
                      questionResult={questionResult}
                    />
                  ))}
                </div>
              )}

            {!areQuestionResultsLoading &&
              !questionResultsError &&
              questionResults &&
              questionResults.length === 0 && (
                <StatusMessage>
                  No question results found for this attempt.
                </StatusMessage>
              )}
          </section>
        )}
      </div>
    </main>
  );
}
