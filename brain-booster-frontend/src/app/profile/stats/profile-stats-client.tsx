"use client";

import { useEffect, useMemo, useState, type ReactNode } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
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
  BarChart3,
  BookOpen,
  Brain,
  CheckCircle2,
  Clock3,
  ClipboardList,
  HelpCircle,
  Layers3,
  MessageCircle,
  Repeat2,
  Target,
  Timer,
  Trophy,
  TrendingUp,
  XCircle,
} from "lucide-react";

import { getUserFlashcardSetsByUserId } from "@/api/userService";
import { useAuth } from "@/context/AuthContext";
import {
  useGameAnalyticsSummary,
  useGameProgress,
  useQuestionTypeAnalytics,
  useWeakFlashcards,
} from "@/hooks/game-results";
import type { GameProgressPoint } from "@/types/games";
import { parseJwt } from "@/utils/jwt";

interface StatsFlashcardSet {
  setId: number;
  name?: string | null;
  title?: string | null;
  setName?: string | null;
}

interface UserFlashcardSetsState {
  data: StatsFlashcardSet[];
  dataKey: string | null;
  error: string | null;
  errorKey: string | null;
}

interface ProgressChartPoint {
  attempt: string;
  completedAt: string;
  shortDate: string;
  percentage: number;
  score: string;
  mode: string;
}

function getAuthenticatedUserId(token: string | null) {
  if (!token) {
    return null;
  }

  const decodedToken = parseJwt(token);

  if (!decodedToken) {
    return null;
  }

  const payload = decodedToken as Record<string, unknown>;
  const rawUserId = payload.userId ?? payload.id ?? payload.user_id;

  if (typeof rawUserId === "number") {
    return rawUserId;
  }

  if (typeof rawUserId === "string") {
    const parsedUserId = Number(rawUserId);

    return Number.isFinite(parsedUserId) ? parsedUserId : null;
  }

  return null;
}

function getErrorMessage(error: unknown) {
  return error instanceof Error
    ? error.message
    : "Failed to fetch flashcard sets.";
}

function getSetDisplayName(set: StatsFlashcardSet) {
  return set.name || set.title || set.setName || `Set #${set.setId}`;
}

function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return "No attempts yet";
  }

  return new Intl.DateTimeFormat("en-US", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

function formatShortDate(value: string) {
  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
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

function formatNumber(value: number | null | undefined) {
  return (value ?? 0).toFixed(2);
}

function clampPercentage(value: number | null | undefined) {
  return Math.min(Math.max(value ?? 0, 0), 100);
}

function useUserFlashcardSets(
  userId: number | null | undefined,
  token: string | null | undefined,
) {
  const queryKey =
    userId !== null && userId !== undefined && token
      ? `user-flashcard-sets:${userId}`
      : null;

  const [state, setState] = useState<UserFlashcardSetsState>({
    data: [],
    dataKey: null,
    error: null,
    errorKey: null,
  });

  const data = queryKey && state.dataKey === queryKey ? state.data : [];
  const error = queryKey && state.errorKey === queryKey ? state.error : null;

  const isLoading = Boolean(
    queryKey && state.dataKey !== queryKey && state.errorKey !== queryKey,
  );

  useEffect(() => {
    if (userId === null || userId === undefined || !token || !queryKey) {
      return;
    }

    const currentUserId = userId;
    const authToken = token;

    let isCancelled = false;

    async function fetchFlashcardSets() {
      try {
        const result = (await getUserFlashcardSetsByUserId(
          currentUserId,
          authToken,
        )) as StatsFlashcardSet[];

        if (isCancelled) {
          return;
        }

        setState({
          data: result,
          dataKey: queryKey,
          error: null,
          errorKey: null,
        });
      } catch (fetchError) {
        if (isCancelled) {
          return;
        }

        setState((previousState) => ({
          ...previousState,
          error: getErrorMessage(fetchError),
          errorKey: queryKey,
        }));
      }
    }

    void fetchFlashcardSets();

    return () => {
      isCancelled = true;
    };
  }, [queryKey, token, userId]);

  return {
    data,
    isLoading,
    error,
  };
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
          ? "flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-amber-100 text-amber-500"
          : "flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-pink-100 text-pink-500"
      }
    >
      {children}
    </div>
  );
}

function ProgressBar({ value }: { value: number | null | undefined }) {
  const clampedValue = clampPercentage(value);

  return (
    <div className="h-2 w-full overflow-hidden rounded-full bg-gray-100">
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
          <h3 className="text-lg font-semibold tracking-tight text-slate-950">
            {title}
          </h3>

          {description ? (
            <p className="mt-1 text-sm text-slate-500">{description}</p>
          ) : null}
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
  helper,
}: {
  icon: ReactNode;
  iconVariant?: "default" | "gold";
  label: string;
  value: ReactNode;
  helper?: string;
}) {
  return (
    <div className="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm transition hover:border-pink-200 hover:shadow-md">
      <div className="h-1 bg-pink-500" />

      <div className="p-5">
        <div className="flex items-start justify-between gap-3">
          <div>
            <p className="text-sm font-medium text-slate-500">{label}</p>
            <p className="mt-3 text-2xl font-bold tracking-tight text-slate-950">
              {value}
            </p>
          </div>

          <IconBox variant={iconVariant}>{icon}</IconBox>
        </div>

        {helper ? (
          <p className="mt-2 text-xs text-slate-400">{helper}</p>
        ) : null}
      </div>
    </div>
  );
}

function ProgressChart({ data }: { data: GameProgressPoint[] }) {
  const chartData: ProgressChartPoint[] = data.map((point, index) => ({
    attempt: `Attempt ${index + 1}`,
    completedAt: formatDateTime(point.completedAt),
    shortDate: formatShortDate(point.completedAt),
    percentage: Math.round(point.percentage),
    score: `${point.score}/${point.totalQuestions}`,
    mode: point.mode,
  }));

  const latestPoint = chartData[chartData.length - 1];
  const bestPercentage = Math.max(
    ...chartData.map((point) => point.percentage),
  );
  const averagePercentage =
    chartData.reduce((sum, point) => sum + point.percentage, 0) /
    chartData.length;

  return (
    <div className="overflow-hidden rounded-3xl border border-gray-200 bg-white shadow-sm">
      <div className="flex flex-col gap-4 border-b border-gray-200 bg-pink-50/70 p-5 md:flex-row md:items-center md:justify-between">
        <div className="flex items-start gap-3">
          <IconBox>
            <TrendingUp size={20} />
          </IconBox>

          <div>
            <h4 className="text-base font-semibold text-slate-950">
              Score percentage trend
            </h4>
            <p className="mt-1 text-sm text-slate-500">
              Each point represents one completed attempt.
            </p>
          </div>
        </div>

        <div className="grid grid-cols-3 gap-3 text-sm">
          <div className="rounded-2xl bg-white px-4 py-3 shadow-sm">
            <div className="flex items-center gap-2 text-xs text-slate-500">
              <Clock3 size={14} />
              <span>Latest</span>
            </div>
            <p className="mt-1 font-semibold text-slate-950">
              {latestPoint ? formatPercentage(latestPoint.percentage) : "0%"}
            </p>
          </div>

          <div className="rounded-2xl bg-white px-4 py-3 shadow-sm">
            <div className="flex items-center gap-2 text-xs text-slate-500">
              <Trophy size={14} className="text-amber-500" />
              <span>Best</span>
            </div>
            <p className="mt-1 font-semibold text-slate-950">
              {formatPercentage(bestPercentage)}
            </p>
          </div>

          <div className="rounded-2xl bg-white px-4 py-3 shadow-sm">
            <div className="flex items-center gap-2 text-xs text-slate-500">
              <BarChart3 size={14} />
              <span>Average</span>
            </div>
            <p className="mt-1 font-semibold text-slate-950">
              {formatPercentage(averagePercentage)}
            </p>
          </div>
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
            <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />

            <XAxis
              dataKey="shortDate"
              tick={{ fill: "#64748b", fontSize: 12 }}
              tickLine={false}
              axisLine={{ stroke: "#e5e7eb" }}
            />

            <YAxis
              domain={[0, 100]}
              tickFormatter={(value) => `${value}%`}
              tick={{ fill: "#64748b", fontSize: 12 }}
              tickLine={false}
              axisLine={{ stroke: "#e5e7eb" }}
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
                borderColor: "#e5e7eb",
                boxShadow: "0 10px 25px rgb(15 23 42 / 0.08)",
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
                fill: "#ffffff",
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

function SelectedSetStats({
  selectedSetId,
  selectedSet,
  token,
}: {
  selectedSetId: number;
  selectedSet?: StatsFlashcardSet;
  token: string;
}) {
  const {
    data: summary,
    isLoading: isSummaryLoading,
    error: summaryError,
  } = useGameAnalyticsSummary(selectedSetId, token);

  const {
    data: progress,
    isLoading: isProgressLoading,
    error: progressError,
  } = useGameProgress(selectedSetId, token);

  const {
    data: weakFlashcards,
    isLoading: areWeakFlashcardsLoading,
    error: weakFlashcardsError,
  } = useWeakFlashcards(selectedSetId, token);

  const {
    data: questionTypes,
    isLoading: areQuestionTypesLoading,
    error: questionTypesError,
  } = useQuestionTypeAnalytics(selectedSetId, token);

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

  const hasAnyError =
    summaryError || progressError || weakFlashcardsError || questionTypesError;

  return (
    <>
      <section className="rounded-3xl border border-gray-200 bg-white p-5 shadow-sm">
        <p className="flex items-center gap-2 text-sm font-medium text-slate-500">
          <Layers3 size={16} />
          Current set
        </p>

        <h2 className="mt-1 text-2xl font-bold tracking-tight text-slate-950">
          {selectedSet
            ? getSetDisplayName(selectedSet)
            : `Set #${selectedSetId}`}
        </h2>

        <p className="mt-2 text-sm text-slate-500">
          These stats are based only on your own game attempts.
        </p>
      </section>

      {isLoadingStats ? (
        <StatusMessage>Loading statistics...</StatusMessage>
      ) : null}

      {summaryError ? (
        <StatusMessage type="error">{summaryError}</StatusMessage>
      ) : null}

      {progressError ? (
        <StatusMessage type="error">{progressError}</StatusMessage>
      ) : null}

      {weakFlashcardsError ? (
        <StatusMessage type="error">{weakFlashcardsError}</StatusMessage>
      ) : null}

      {questionTypesError ? (
        <StatusMessage type="error">{questionTypesError}</StatusMessage>
      ) : null}

      {!isLoadingStats && !hasAnyError && !hasAnyStats ? (
        <section className="rounded-3xl border border-dashed border-gray-200 bg-white p-10 text-center shadow-sm">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-pink-100 text-pink-500">
            <Brain size={28} />
          </div>

          <h2 className="mt-4 text-xl font-semibold text-slate-950">
            No statistics yet
          </h2>

          <p className="mx-auto mt-2 max-w-md text-sm text-slate-500">
            Complete at least one game in this flashcard set to unlock summary,
            progress, weak flashcards, and question type analytics.
          </p>
        </section>
      ) : null}

      {hasSummaryData && summary ? (
        <SectionCard
          icon={<ClipboardList size={20} />}
          title="Summary"
          description="A quick overview of your learning performance in this set."
        >
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <SummaryCard
              icon={<Repeat2 size={20} />}
              label="Total attempts"
              value={summary.totalAttempts}
            />

            <SummaryCard
              icon={<Target size={20} />}
              label="Accuracy"
              value={formatPercentage(summary.accuracyPercentage)}
            />

            <SummaryCard
              icon={<BarChart3 size={20} />}
              label="Average score"
              value={formatNumber(summary.averageScore)}
            />

            <SummaryCard
              icon={<Trophy size={20} />}
              iconVariant="gold"
              label="Best score"
              value={summary.bestScore}
            />

            <SummaryCard
              icon={<Timer size={20} />}
              label="Average duration"
              value={formatDuration(Math.round(summary.averageDuration))}
            />

            <SummaryCard
              icon={<Clock3 size={20} />}
              label="Last attempt"
              value={formatDateTime(summary.lastAttemptAt)}
            />
          </div>
        </SectionCard>
      ) : null}

      {hasProgressData && progress ? (
        <SectionCard
          icon={<TrendingUp size={20} />}
          title="Progress"
          description="Your latest attempts and score percentage over time."
        >
          <div className="space-y-4">
            <ProgressChart data={progress} />

            <div className="overflow-hidden rounded-3xl border border-gray-200 bg-white shadow-sm">
              <div className="overflow-x-auto">
                <table className="w-full min-w-[860px] text-sm">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-5 py-4 text-left font-semibold text-slate-950">
                        Completed at
                      </th>
                      <th className="px-5 py-4 text-left font-semibold text-slate-950">
                        Mode
                      </th>
                      <th className="px-5 py-4 text-left font-semibold text-slate-950">
                        Score
                      </th>
                      <th className="px-5 py-4 text-left font-semibold text-slate-950">
                        Percentage
                      </th>
                      <th className="px-5 py-4 text-left font-semibold text-slate-950">
                        Duration
                      </th>
                      <th className="px-5 py-4 text-left font-semibold text-slate-950">
                        Details
                      </th>
                    </tr>
                  </thead>

                  <tbody>
                    {progress.map((point, index) => (
                      <tr
                        key={`${point.completedAt}-${point.mode}-${index}`}
                        className="border-t border-gray-200 transition hover:bg-pink-50/40"
                      >
                        <td className="px-5 py-4 text-slate-500">
                          {formatDateTime(point.completedAt)}
                        </td>

                        <td className="px-5 py-4">
                          <span className="rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500">
                            {point.mode}
                          </span>
                        </td>

                        <td className="px-5 py-4 font-medium text-slate-950">
                          {point.score}/{point.totalQuestions}
                        </td>

                        <td className="px-5 py-4">
                          <div className="flex min-w-40 items-center gap-3">
                            <ProgressBar value={point.percentage} />
                            <span className="w-16 text-right font-medium text-slate-950">
                              {formatPercentage(point.percentage)}
                            </span>
                          </div>
                        </td>

                        <td className="px-5 py-4 text-slate-500">
                          {formatDuration(point.durationSeconds)}
                        </td>

                        <td className="px-5 py-4">
                          <Link
                            href={`/profile/attempts/${point.attemptId}`}
                            className="inline-flex items-center gap-2 rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500 transition hover:bg-pink-200"
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
          </div>
        </SectionCard>
      ) : null}

      {hasWeakFlashcardsData && weakFlashcards ? (
        <SectionCard
          icon={<Brain size={20} />}
          title="Weak flashcards"
          description="Cards that need more practice based on mistakes and incorrect answers."
        >
          <div className="grid gap-4 md:grid-cols-2">
            {weakFlashcards.map((flashcard) => (
              <article
                key={flashcard.flashcardId}
                className="overflow-hidden rounded-3xl border border-gray-200 bg-white shadow-sm transition hover:border-pink-200 hover:shadow-md"
              >
                <div className="h-1 bg-pink-500" />

                <div className="p-5">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <div className="flex items-center gap-2">
                        <BookOpen
                          size={18}
                          className="shrink-0 text-pink-500"
                        />
                        <p className="text-lg font-semibold text-slate-950">
                          {flashcard.term}
                        </p>
                      </div>

                      <p className="mt-1 text-sm text-slate-500">
                        {flashcard.definition}
                      </p>
                    </div>

                    <span className="rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500">
                      {formatPercentage(flashcard.accuracyPercentage)}
                    </span>
                  </div>

                  <div className="mt-5">
                    <ProgressBar value={flashcard.accuracyPercentage} />
                  </div>

                  <div className="mt-5 grid grid-cols-2 gap-3 text-sm">
                    <div className="rounded-2xl bg-gray-50 p-3">
                      <div className="flex items-center gap-2 text-red-600">
                        <XCircle size={15} />
                        <span>Incorrect</span>
                      </div>
                      <p className="mt-1 font-semibold text-slate-950">
                        {flashcard.incorrectAnswers}
                      </p>
                    </div>

                    <div className="rounded-2xl bg-gray-50 p-3">
                      <div className="flex items-center gap-2 text-slate-500">
                        <HelpCircle size={15} />
                        <span>Mistakes</span>
                      </div>
                      <p className="mt-1 font-semibold text-slate-950">
                        {flashcard.totalMistakes}
                      </p>
                    </div>

                    <div className="rounded-2xl bg-gray-50 p-3">
                      <div className="flex items-center gap-2 text-green-600">
                        <CheckCircle2 size={15} />
                        <span>Correct</span>
                      </div>
                      <p className="mt-1 font-semibold text-slate-950">
                        {flashcard.correctAnswers}
                      </p>
                    </div>

                    <div className="rounded-2xl bg-gray-50 p-3">
                      <div className="flex items-center gap-2 text-slate-500">
                        <MessageCircle size={15} />
                        <span>Answers</span>
                      </div>
                      <p className="mt-1 font-semibold text-slate-950">
                        {flashcard.totalAnswers}
                      </p>
                    </div>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </SectionCard>
      ) : null}

      {hasQuestionTypesData && questionTypes ? (
        <SectionCard
          icon={<Layers3 size={20} />}
          title="Question type accuracy"
          description="Compare how well you perform in each question type."
        >
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {questionTypes.map((questionType) => (
              <div
                key={questionType.questionType}
                className="rounded-3xl border border-gray-200 bg-white p-5 shadow-sm transition hover:border-pink-200 hover:shadow-md"
              >
                <div className="flex items-center gap-2 text-sm font-medium text-slate-500">
                  <Layers3 size={16} className="text-pink-500" />
                  <span>{questionType.questionType}</span>
                </div>

                <p className="mt-3 text-2xl font-bold text-slate-950">
                  {formatPercentage(questionType.accuracyPercentage)}
                </p>

                <div className="mt-4">
                  <ProgressBar value={questionType.accuracyPercentage} />
                </div>

                <p className="mt-3 text-xs text-slate-400">
                  {questionType.correctAnswers}/{questionType.totalAnswers}{" "}
                  correct
                </p>
              </div>
            ))}
          </div>
        </SectionCard>
      ) : null}
    </>
  );
}

export default function ProfileStatsClient() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { token, isAuthenticated, isAuthLoading } = useAuth();

  const userId = useMemo(() => getAuthenticatedUserId(token), [token]);

  const selectedSetId = useMemo(() => {
    const rawSetId = searchParams.get("setId");

    if (!rawSetId) {
      return null;
    }

    const parsedSetId = Number(rawSetId);

    return Number.isFinite(parsedSetId) && parsedSetId > 0 ? parsedSetId : null;
  }, [searchParams]);

  const {
    data: flashcardSets,
    isLoading: areSetsLoading,
    error: setsError,
  } = useUserFlashcardSets(userId, token);

  const selectedSet = flashcardSets.find(
    (flashcardSet) => flashcardSet.setId === selectedSetId,
  );

  function handleSelectedSetChange(nextSetId: string) {
    if (!nextSetId) {
      router.replace("/profile/stats");
      return;
    }

    router.replace(`/profile/stats?setId=${nextSetId}`);
  }

  if (isAuthLoading) {
    return (
      <main className="min-h-[calc(100vh-4rem)] bg-gray-50">
        <div className="mx-auto max-w-6xl px-4 py-10">
          <StatusMessage>Loading stats...</StatusMessage>
        </div>
      </main>
    );
  }

  if (!token || !isAuthenticated) {
    return (
      <main className="min-h-[calc(100vh-4rem)] bg-gray-50">
        <div className="mx-auto max-w-6xl px-4 py-10">
          <section className="overflow-hidden rounded-3xl border border-gray-200 bg-white shadow-sm">
            <div className="bg-pink-50/70 p-8">
              <p className="mb-3 inline-flex items-center gap-2 rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500">
                <BarChart3 size={14} />
                Brain Booster Stats
              </p>

              <h1 className="text-3xl font-bold tracking-tight text-slate-950">
                Learning statistics
              </h1>

              <p className="mt-3 max-w-2xl text-slate-500">
                You need to be logged in to view your learning statistics.
              </p>
            </div>
          </section>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-[calc(100vh-4rem)] bg-gray-50">
      <div className="mx-auto max-w-6xl space-y-8 px-4 py-8">
        <section className="overflow-hidden rounded-3xl border border-gray-200 bg-white shadow-sm">
          <div className="bg-pink-50/70 p-6 sm:p-8">
            <p className="mb-3 inline-flex items-center gap-2 rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500">
              <BarChart3 size={14} />
              Brain Booster Stats
            </p>

            <h1 className="text-3xl font-bold tracking-tight text-slate-950 sm:text-4xl">
              Learning statistics
            </h1>

            <p className="mt-3 flex max-w-2xl items-start gap-2 text-slate-500">
              <TrendingUp size={18} className="mt-0.5 shrink-0 text-pink-500" />
              <span>
                Track your progress, discover weak flashcards, and compare your
                accuracy across game modes.
              </span>
            </p>
          </div>
        </section>

        <section className="rounded-3xl border border-gray-200 bg-white p-5 shadow-sm">
          <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
            <div className="space-y-1">
              <p className="flex items-center gap-2 text-sm font-semibold text-pink-500">
                <BookOpen size={16} />
                Flashcard set
              </p>

              <h2 className="text-xl font-semibold tracking-tight text-slate-950">
                Choose learning material
              </h2>

              <p className="text-sm text-slate-500">
                Analytics are calculated only from your own attempts.
              </p>
            </div>

            <div className="w-full md:max-w-md">
              <label
                htmlFor="setId"
                className="text-sm font-medium text-slate-500"
              >
                Select flashcard set
              </label>

              <select
                id="setId"
                value={selectedSetId ? String(selectedSetId) : ""}
                onChange={(event) =>
                  handleSelectedSetChange(event.target.value)
                }
                disabled={areSetsLoading || flashcardSets.length === 0}
                className="mt-2 w-full rounded-xl border border-gray-200 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm outline-none transition focus:border-pink-400 focus:ring-4 focus:ring-pink-100 disabled:cursor-not-allowed disabled:opacity-60"
              >
                <option value="">
                  {areSetsLoading ? "Loading sets..." : "Choose a set"}
                </option>

                {flashcardSets.map((flashcardSet) => (
                  <option key={flashcardSet.setId} value={flashcardSet.setId}>
                    {getSetDisplayName(flashcardSet)}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {setsError ? (
            <div className="mt-4">
              <StatusMessage type="error">{setsError}</StatusMessage>
            </div>
          ) : null}

          {!areSetsLoading && flashcardSets.length === 0 ? (
            <div className="mt-4">
              <StatusMessage>
                You do not have any flashcard sets yet.
              </StatusMessage>
            </div>
          ) : null}
        </section>

        {selectedSetId ? (
          <SelectedSetStats
            selectedSetId={selectedSetId}
            selectedSet={selectedSet}
            token={token}
          />
        ) : (
          <section className="rounded-3xl border border-dashed border-gray-200 bg-white p-10 text-center shadow-sm">
            <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-pink-100 text-pink-500">
              <Brain size={28} />
            </div>

            <h2 className="mt-4 text-xl font-semibold text-slate-950">
              Select a set to unlock stats
            </h2>

            <p className="mx-auto mt-2 max-w-md text-sm text-slate-500">
              Choose one of your flashcard sets above to see progress, weak
              cards, and question type accuracy.
            </p>
          </section>
        )}
      </div>
    </main>
  );
}
