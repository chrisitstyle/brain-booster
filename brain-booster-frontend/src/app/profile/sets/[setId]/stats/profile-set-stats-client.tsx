"use client";

import type { ReactNode } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import {
  AlertCircle,
  ArrowLeft,
  BarChart3,
  BookOpen,
  Brain,
  CheckCircle2,
  Clock3,
  ClipboardList,
  HelpCircle,
  Layers3,
  Loader2,
  MessageCircle,
  Repeat2,
  Target,
  Timer,
  Trophy,
  TrendingUp,
  XCircle,
} from "lucide-react";

import { useAuth } from "@/context/AuthContext";
import {
  useGameAnalyticsSummary,
  useGameProgress,
  useQuestionTypeAnalytics,
  useWeakFlashcards,
} from "@/hooks/game-results";
import {
  getSetDisplayName,
  useUserFlashcardSets,
} from "@/hooks/use-user-flashcard-sets";
import type { GameProgressPoint } from "@/types/games";

interface ProfileSetStatsClientProps {
  setId: number;
}

interface ProgressChartPoint {
  attempt: string;
  completedAt: string;
  percentage: number;
  score: string;
  mode: string;
}

function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return "No attempts yet";
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
  const normalizedSeconds = Math.max(0, Math.round(seconds ?? 0));

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

function formatNumber(value: number | null | undefined) {
  const normalizedValue =
    typeof value === "number" && Number.isFinite(value) ? value : 0;

  return normalizedValue.toFixed(2);
}

function IconBox({
  children,
  variant = "default",
}: {
  children: ReactNode;
  variant?: "default" | "gold";
}) {
  return (
    <div
      className={
        variant === "gold"
          ? "flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-amber-100 text-amber-600 dark:bg-amber-950/50 dark:text-amber-400"
          : "flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-pink-100 text-pink-500 dark:bg-pink-950/50 dark:text-pink-400"
      }
    >
      {children}
    </div>
  );
}

function ProgressBar({ value }: { value: number | null | undefined }) {
  const clampedValue = clampPercentage(value);

  return (
    <div
      className="h-2 w-full overflow-hidden rounded-full bg-muted"
      role="progressbar"
      aria-valuemin={0}
      aria-valuemax={100}
      aria-valuenow={Math.round(clampedValue)}
    >
      <div
        className="h-full rounded-full bg-pink-500 transition-all"
        style={{ width: `${clampedValue}%` }}
      />
    </div>
  );
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

function SectionCard({
  icon,
  title,
  description,
  children,
}: {
  icon: ReactNode;
  title: string;
  description?: string;
  children: ReactNode;
}) {
  return (
    <section className="space-y-4">
      <div className="flex items-start gap-3">
        <IconBox>{icon}</IconBox>

        <div>
          <h2 className="text-lg font-semibold tracking-tight text-foreground">
            {title}
          </h2>

          {description && (
            <p className="mt-1 text-sm text-muted-foreground">{description}</p>
          )}
        </div>
      </div>

      {children}
    </section>
  );
}

function SummaryCard({
  icon,
  iconVariant = "default",
  label,
  value,
}: {
  icon: ReactNode;
  iconVariant?: "default" | "gold";
  label: string;
  value: ReactNode;
}) {
  return (
    <div className="overflow-hidden rounded-2xl border border-border bg-card text-card-foreground shadow-sm transition-all hover:border-pink-200 hover:shadow-md dark:hover:border-pink-900">
      <div className="h-1 bg-pink-500" />

      <div className="p-5">
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0">
            <p className="text-sm font-medium text-muted-foreground">{label}</p>

            <p className="mt-3 break-words text-2xl font-bold tracking-tight text-card-foreground">
              {value}
            </p>
          </div>

          <IconBox variant={iconVariant}>{icon}</IconBox>
        </div>
      </div>
    </div>
  );
}

function ProgressChart({ data }: { data: GameProgressPoint[] }) {
  const sortedData = [...data].sort(
    (firstPoint, secondPoint) =>
      new Date(firstPoint.completedAt).getTime() -
      new Date(secondPoint.completedAt).getTime(),
  );

  const chartData: ProgressChartPoint[] = sortedData.map((point, index) => ({
    attempt: `Attempt ${index + 1}`,
    completedAt: formatDateTime(point.completedAt),
    percentage: Math.round(clampPercentage(point.percentage)),
    score: `${point.score}/${point.totalQuestions}`,
    mode: point.mode,
  }));

  const latestPoint = chartData.at(-1);

  const bestPercentage =
    chartData.length > 0
      ? Math.max(...chartData.map((point) => point.percentage))
      : 0;

  const averagePercentage =
    chartData.length > 0
      ? chartData.reduce((sum, point) => sum + point.percentage, 0) /
        chartData.length
      : 0;

  return (
    <div className="overflow-hidden rounded-3xl border border-border bg-card text-card-foreground shadow-sm">
      <div className="flex flex-col gap-4 border-b border-border bg-pink-50/70 p-5 dark:bg-pink-950/20 md:flex-row md:items-center md:justify-between">
        <div className="flex items-start gap-3">
          <IconBox>
            <TrendingUp size={20} aria-hidden="true" />
          </IconBox>

          <div>
            <h3 className="text-base font-semibold text-card-foreground">
              Score percentage trend
            </h3>

            <p className="mt-1 text-sm text-muted-foreground">
              Each point represents one completed attempt.
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 gap-3 text-sm sm:grid-cols-3">
          <ChartSummaryValue
            icon={<Clock3 size={14} aria-hidden="true" />}
            label="Latest"
            value={formatPercentage(latestPoint?.percentage)}
          />

          <ChartSummaryValue
            icon={
              <Trophy
                size={14}
                className="text-amber-500 dark:text-amber-400"
                aria-hidden="true"
              />
            }
            label="Best"
            value={formatPercentage(bestPercentage)}
          />

          <ChartSummaryValue
            icon={<BarChart3 size={14} aria-hidden="true" />}
            label="Average"
            value={formatPercentage(averagePercentage)}
          />
        </div>
      </div>

      <div className="h-80 p-5">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart
            data={chartData}
            margin={{
              top: 12,
              right: 16,
              left: 0,
              bottom: 8,
            }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />

            <XAxis
              dataKey="attempt"
              interval={0}
              tick={{
                fill: "var(--muted-foreground)",
                fontSize: 12,
              }}
              tickLine={false}
              axisLine={{
                stroke: "var(--border)",
              }}
            />

            <YAxis
              domain={[0, 100]}
              tickFormatter={(value) => `${value}%`}
              tick={{
                fill: "var(--muted-foreground)",
                fontSize: 12,
              }}
              tickLine={false}
              axisLine={{
                stroke: "var(--border)",
              }}
            />

            <Tooltip
              formatter={(value, name) => {
                if (name === "percentage") {
                  return [`${Math.round(Number(value))}%`, "Score"];
                }

                return [value, name];
              }}
              labelFormatter={(_, payload) => {
                const point = payload?.[0]?.payload as
                  | ProgressChartPoint
                  | undefined;

                if (!point) {
                  return "";
                }

                return `${point.attempt} • ${point.completedAt}`;
              }}
              contentStyle={{
                borderRadius: "16px",
                borderColor: "var(--border)",
                backgroundColor: "var(--popover)",
                color: "var(--popover-foreground)",
                boxShadow: "0 10px 25px rgb(0 0 0 / 0.15)",
              }}
              itemStyle={{
                color: "var(--popover-foreground)",
              }}
              labelStyle={{
                color: "var(--popover-foreground)",
              }}
            />

            <Line
              type="monotone"
              dataKey="percentage"
              stroke="#ec4899"
              strokeWidth={3}
              dot={{
                r: 4,
                strokeWidth: 2,
                stroke: "#ec4899",
                fill: "var(--card)",
              }}
              activeDot={{
                r: 6,
                strokeWidth: 2,
                stroke: "#ec4899",
                fill: "#ec4899",
              }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

function ChartSummaryValue({
  icon,
  label,
  value,
}: {
  icon: ReactNode;
  label: string;
  value: string;
}) {
  return (
    <div className="rounded-2xl border border-border/70 bg-card px-4 py-3 shadow-sm">
      <div className="flex items-center gap-2 text-xs text-muted-foreground">
        {icon}
        <span>{label}</span>
      </div>

      <p className="mt-1 font-semibold text-card-foreground">{value}</p>
    </div>
  );
}

export default function ProfileSetStatsClient({
  setId,
}: ProfileSetStatsClientProps) {
  const router = useRouter();

  const { token, isAuthenticated, isAuthLoading } = useAuth();

  const {
    data: flashcardSets,
    isLoading: areSetsLoading,
    error: setsError,
  } = useUserFlashcardSets(token);

  const {
    data: summary,
    isLoading: isSummaryLoading,
    error: summaryError,
  } = useGameAnalyticsSummary(setId, token);

  const {
    data: progress,
    isLoading: isProgressLoading,
    error: progressError,
  } = useGameProgress(setId, token);

  const {
    data: weakFlashcards,
    isLoading: areWeakFlashcardsLoading,
    error: weakFlashcardsError,
  } = useWeakFlashcards(setId, token);

  const {
    data: questionTypes,
    isLoading: areQuestionTypesLoading,
    error: questionTypesError,
  } = useQuestionTypeAnalytics(setId, token);

  const selectedSet = flashcardSets.find(
    (flashcardSet) => flashcardSet.setId === setId,
  );

  const hasCurrentSetOption = flashcardSets.some(
    (flashcardSet) => flashcardSet.setId === setId,
  );

  const isLoadingStats =
    isSummaryLoading ||
    isProgressLoading ||
    areWeakFlashcardsLoading ||
    areQuestionTypesLoading;

  const hasSummaryData = Boolean(summary && summary.totalAttempts > 0);

  const hasProgressData = Boolean(progress && progress.length > 0);

  const hasWeakFlashcardsData = Boolean(
    weakFlashcards && weakFlashcards.length > 0,
  );

  const hasQuestionTypesData = Boolean(
    questionTypes && questionTypes.length > 0,
  );

  const hasAnyStats =
    hasSummaryData ||
    hasProgressData ||
    hasWeakFlashcardsData ||
    hasQuestionTypesData;

  const hasAnyError = Boolean(
    summaryError || progressError || weakFlashcardsError || questionTypesError,
  );

  function handleSelectedSetChange(nextSetId: string) {
    const parsedSetId = Number(nextSetId);

    if (!Number.isInteger(parsedSetId) || parsedSetId <= 0) {
      return;
    }

    router.push(`/profile/sets/${parsedSetId}/stats`);
  }

  if (isAuthLoading) {
    return (
      <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
        <div className="mx-auto max-w-6xl px-4 py-10">
          <StatusMessage type="loading">Loading statistics...</StatusMessage>
        </div>
      </main>
    );
  }

  if (!token || !isAuthenticated) {
    return (
      <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
        <div className="mx-auto max-w-6xl px-4 py-10">
          <StatusMessage type="error">
            You need to be logged in to view statistics.
          </StatusMessage>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
      <div className="mx-auto max-w-6xl space-y-8 px-4 py-8">
        <Link
          href="/profile/stats"
          className="inline-flex items-center gap-2 text-sm font-medium text-muted-foreground transition-colors hover:text-pink-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring dark:hover:text-pink-400"
        >
          <ArrowLeft size={16} aria-hidden="true" />
          Back to My Stats
        </Link>

        <section className="overflow-hidden rounded-3xl border border-border bg-card text-card-foreground shadow-sm">
          <div className="bg-pink-50/70 p-6 dark:bg-pink-950/20 sm:p-8">
            <p className="mb-3 inline-flex items-center gap-2 rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
              <TrendingUp size={14} aria-hidden="true" />
              Brain Booster Stats
            </p>

            <h1 className="text-3xl font-bold tracking-tight text-card-foreground sm:text-4xl">
              Learning statistics
            </h1>

            <p className="mt-3 max-w-2xl text-muted-foreground">
              Track your progress, discover weak flashcards, and compare your
              accuracy across question types.
            </p>
          </div>
        </section>

        <section className="rounded-3xl border border-border bg-card p-5 text-card-foreground shadow-sm">
          <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
            <div>
              <p className="flex items-center gap-2 text-sm font-semibold text-pink-500 dark:text-pink-400">
                <BookOpen size={16} aria-hidden="true" />
                Current flashcard set
              </p>

              <h2 className="mt-1 break-words text-2xl font-bold tracking-tight text-card-foreground">
                {selectedSet ? getSetDisplayName(selectedSet) : `Set #${setId}`}
              </h2>

              <p className="mt-2 text-sm text-muted-foreground">
                These statistics are calculated only from your own attempts.
              </p>
            </div>

            <div className="w-full md:max-w-md">
              <label
                htmlFor="stats-set-id"
                className="text-sm font-medium text-muted-foreground"
              >
                Change flashcard set
              </label>

              <select
                id="stats-set-id"
                value={String(setId)}
                onChange={(event) =>
                  handleSelectedSetChange(event.target.value)
                }
                disabled={areSetsLoading || flashcardSets.length === 0}
                className="mt-2 w-full rounded-xl border border-input bg-background px-4 py-3 text-sm text-foreground shadow-sm outline-none transition focus:border-pink-400 focus:ring-4 focus:ring-pink-500/15 disabled:cursor-not-allowed disabled:bg-muted disabled:text-muted-foreground"
              >
                {!hasCurrentSetOption && (
                  <option value={setId}>Set #{setId}</option>
                )}

                {flashcardSets.map((flashcardSet) => (
                  <option key={flashcardSet.setId} value={flashcardSet.setId}>
                    {getSetDisplayName(flashcardSet)}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {setsError && (
            <div className="mt-4">
              <StatusMessage type="error">{setsError}</StatusMessage>
            </div>
          )}
        </section>

        {isLoadingStats && (
          <StatusMessage type="loading">Loading statistics...</StatusMessage>
        )}

        {summaryError && (
          <StatusMessage type="error">{summaryError}</StatusMessage>
        )}

        {progressError && (
          <StatusMessage type="error">{progressError}</StatusMessage>
        )}

        {weakFlashcardsError && (
          <StatusMessage type="error">{weakFlashcardsError}</StatusMessage>
        )}

        {questionTypesError && (
          <StatusMessage type="error">{questionTypesError}</StatusMessage>
        )}

        {!isLoadingStats && !hasAnyError && !hasAnyStats && (
          <section className="rounded-3xl border border-dashed border-border bg-card p-10 text-center text-card-foreground shadow-sm">
            <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-pink-100 text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
              <Brain size={28} aria-hidden="true" />
            </div>

            <h2 className="mt-4 text-xl font-semibold text-card-foreground">
              No statistics yet
            </h2>

            <p className="mx-auto mt-2 max-w-md text-sm text-muted-foreground">
              Complete at least one game in this flashcard set to unlock
              statistics.
            </p>
          </section>
        )}

        {hasSummaryData && summary && (
          <SectionCard
            icon={<ClipboardList size={20} aria-hidden="true" />}
            title="Summary"
            description="A quick overview of your learning performance."
          >
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <SummaryCard
                icon={<Repeat2 size={20} aria-hidden="true" />}
                label="Total attempts"
                value={summary.totalAttempts}
              />

              <SummaryCard
                icon={<Target size={20} aria-hidden="true" />}
                label="Accuracy"
                value={formatPercentage(summary.accuracyPercentage)}
              />

              <SummaryCard
                icon={<BarChart3 size={20} aria-hidden="true" />}
                label="Average score"
                value={formatNumber(summary.averageScore)}
              />

              <SummaryCard
                icon={<Trophy size={20} aria-hidden="true" />}
                iconVariant="gold"
                label="Best score"
                value={summary.bestScore}
              />

              <SummaryCard
                icon={<Timer size={20} aria-hidden="true" />}
                label="Average duration"
                value={formatDuration(summary.averageDuration)}
              />

              <SummaryCard
                icon={<Clock3 size={20} aria-hidden="true" />}
                label="Last attempt"
                value={formatDateTime(summary.lastAttemptAt)}
              />
            </div>
          </SectionCard>
        )}

        {hasProgressData && progress && (
          <SectionCard
            icon={<TrendingUp size={20} aria-hidden="true" />}
            title="Progress"
            description="Your completed attempts and score percentage over time."
          >
            <div className="space-y-4">
              <ProgressChart data={progress} />

              <div className="overflow-hidden rounded-3xl border border-border bg-card text-card-foreground shadow-sm">
                <div className="overflow-x-auto">
                  <table className="w-full min-w-[860px] text-sm">
                    <thead className="bg-muted/50">
                      <tr>
                        <TableHeader>Completed at</TableHeader>
                        <TableHeader>Mode</TableHeader>
                        <TableHeader>Score</TableHeader>
                        <TableHeader>Percentage</TableHeader>
                        <TableHeader>Duration</TableHeader>
                        <TableHeader>Details</TableHeader>
                      </tr>
                    </thead>

                    <tbody>
                      {progress.map((point) => (
                        <tr
                          key={point.attemptId}
                          className="border-t border-border transition-colors hover:bg-pink-50/40 dark:hover:bg-pink-950/20"
                        >
                          <td className="px-5 py-4 text-muted-foreground">
                            {formatDateTime(point.completedAt)}
                          </td>

                          <td className="px-5 py-4">
                            <span className="rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
                              {point.mode}
                            </span>
                          </td>

                          <td className="px-5 py-4 font-medium text-card-foreground">
                            {point.score}/{point.totalQuestions}
                          </td>

                          <td className="px-5 py-4">
                            <div className="flex min-w-40 items-center gap-3">
                              <ProgressBar value={point.percentage} />

                              <span className="w-16 text-right font-medium text-card-foreground">
                                {formatPercentage(point.percentage)}
                              </span>
                            </div>
                          </td>

                          <td className="px-5 py-4 text-muted-foreground">
                            {formatDuration(point.durationSeconds)}
                          </td>

                          <td className="px-5 py-4">
                            <Link
                              href={`/profile/sets/${setId}/attempts/${point.attemptId}`}
                              className="inline-flex items-center gap-2 rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500 transition-colors hover:bg-pink-200 dark:bg-pink-950/50 dark:text-pink-400 dark:hover:bg-pink-900/60"
                            >
                              View details
                            </Link>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>

              <div className="flex justify-end">
                <Link
                  href={`/profile/sets/${setId}/attempts`}
                  className="inline-flex items-center gap-2 rounded-xl border border-border bg-card px-4 py-2.5 text-sm font-semibold text-pink-500 transition-colors hover:border-pink-200 hover:bg-pink-50 dark:text-pink-400 dark:hover:border-pink-900 dark:hover:bg-pink-950/30"
                >
                  <ClipboardList size={16} aria-hidden="true" />
                  View all attempts
                </Link>
              </div>
            </div>
          </SectionCard>
        )}

        {hasWeakFlashcardsData && weakFlashcards && (
          <SectionCard
            icon={<Brain size={20} aria-hidden="true" />}
            title="Weak flashcards"
            description="Cards that need more practice based on your incorrect answers."
          >
            <div className="flex flex-col gap-4 rounded-3xl border border-pink-200 bg-pink-50 p-5 dark:border-pink-900 dark:bg-pink-950/20 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h3 className="font-semibold text-foreground">
                  Review your weak cards
                </h3>

                <p className="mt-1 text-sm text-muted-foreground">
                  Start a focused review using only these flashcards.
                </p>
              </div>

              <Link
                href={`/profile/sets/${setId}/weak-cards`}
                className="inline-flex shrink-0 items-center justify-center gap-2 rounded-xl bg-pink-500 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-pink-600"
              >
                <BookOpen size={16} aria-hidden="true" />
                Review weak cards
              </Link>
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              {weakFlashcards.map((flashcard) => (
                <article
                  key={flashcard.flashcardId}
                  className="overflow-hidden rounded-3xl border border-border bg-card text-card-foreground shadow-sm"
                >
                  <div className="h-1 bg-pink-500" />

                  <div className="p-5">
                    <div className="flex items-start justify-between gap-4">
                      <div className="min-w-0">
                        <p className="break-words text-lg font-semibold text-card-foreground">
                          {flashcard.term}
                        </p>

                        <p className="mt-1 break-words text-sm text-muted-foreground">
                          {flashcard.definition}
                        </p>
                      </div>

                      <span className="shrink-0 rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
                        {formatPercentage(flashcard.accuracyPercentage)}
                      </span>
                    </div>

                    <div className="mt-5">
                      <ProgressBar value={flashcard.accuracyPercentage} />
                    </div>

                    <div className="mt-5 grid grid-cols-2 gap-3 text-sm">
                      <WeakCardMetric
                        icon={<XCircle size={15} aria-hidden="true" />}
                        label="Incorrect"
                        value={flashcard.incorrectAnswers}
                        variant="error"
                      />

                      <WeakCardMetric
                        icon={<HelpCircle size={15} aria-hidden="true" />}
                        label="Mistakes"
                        value={flashcard.totalMistakes}
                      />

                      <WeakCardMetric
                        icon={<CheckCircle2 size={15} aria-hidden="true" />}
                        label="Correct"
                        value={flashcard.correctAnswers}
                        variant="success"
                      />

                      <WeakCardMetric
                        icon={<MessageCircle size={15} aria-hidden="true" />}
                        label="Answers"
                        value={flashcard.totalAnswers}
                      />
                    </div>
                  </div>
                </article>
              ))}
            </div>
          </SectionCard>
        )}

        {hasQuestionTypesData && questionTypes && (
          <SectionCard
            icon={<Layers3 size={20} aria-hidden="true" />}
            title="Question type accuracy"
            description="Compare how well you perform in each question type."
          >
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              {questionTypes.map((questionType) => (
                <div
                  key={questionType.questionType}
                  className="rounded-3xl border border-border bg-card p-5 text-card-foreground shadow-sm"
                >
                  <div className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
                    <Layers3
                      size={16}
                      className="text-pink-500 dark:text-pink-400"
                      aria-hidden="true"
                    />

                    {questionType.questionType}
                  </div>

                  <p className="mt-3 text-2xl font-bold text-card-foreground">
                    {formatPercentage(questionType.accuracyPercentage)}
                  </p>

                  <div className="mt-4">
                    <ProgressBar value={questionType.accuracyPercentage} />
                  </div>

                  <p className="mt-3 text-xs text-muted-foreground">
                    {questionType.correctAnswers}/{questionType.totalAnswers}{" "}
                    correct
                  </p>
                </div>
              ))}
            </div>
          </SectionCard>
        )}
      </div>
    </main>
  );
}

function TableHeader({ children }: { children: ReactNode }) {
  return (
    <th className="px-5 py-4 text-left font-semibold text-foreground">
      {children}
    </th>
  );
}

function WeakCardMetric({
  icon,
  label,
  value,
  variant = "default",
}: {
  icon: ReactNode;
  label: string;
  value: number;
  variant?: "default" | "error" | "success";
}) {
  const labelClassName =
    variant === "error"
      ? "text-red-600 dark:text-red-400"
      : variant === "success"
        ? "text-green-600 dark:text-green-400"
        : "text-muted-foreground";

  return (
    <div className="rounded-2xl bg-muted/60 p-3">
      <div className={`flex items-center gap-2 ${labelClassName}`}>
        {icon}
        {label}
      </div>

      <p className="mt-1 font-semibold text-foreground">{value}</p>
    </div>
  );
}
