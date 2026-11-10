"use client";

import { useEffect, useState, type ReactNode } from "react";
import Link from "next/link";
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import {
  AlertCircle,
  ArrowLeft,
  BookOpen,
  BrainCircuit,
  CheckCircle2,
  Clock3,
  Layers3,
  Loader2,
  RefreshCw,
  Settings2,
  Sparkles,
  type LucideIcon,
} from "lucide-react";

import { getReviewDashboard } from "@/api/spacedRepetitionService";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/context/AuthContext";
import type { ReviewDashboard, ReviewRating } from "@/types/spaced-repetition";

type DashboardState =
  | { status: "loading" }
  | {
      status: "ready";
      token: string;
      reloadCounter: number;
      data: ReviewDashboard;
    }
  | {
      status: "error";
      token: string;
      reloadCounter: number;
      message: string;
    };

type StageKey =
  | "newCards"
  | "learningCards"
  | "reviewCards"
  | "relearningCards";

const STAGES: {
  key: StageKey;
  label: string;
  description: string;
  color: string;
}[] = [
  {
    key: "newCards",
    label: "New",
    description: "Not introduced yet",
    color: "#94a3b8",
  },
  {
    key: "learningCards",
    label: "Learning",
    description: "Short-term learning",
    color: "#f59e0b",
  },
  {
    key: "reviewCards",
    label: "Review",
    description: "Long-term schedule",
    color: "#ec4899",
  },
  {
    key: "relearningCards",
    label: "Relearning",
    description: "Learning again",
    color: "#8b5cf6",
  },
];

const RATINGS: {
  rating: ReviewRating;
  label: string;
  color: string;
}[] = [
  { rating: "AGAIN", label: "Again", color: "#ef4444" },
  { rating: "HARD", label: "Hard", color: "#f59e0b" },
  { rating: "GOOD", label: "Good", color: "#22c55e" },
  { rating: "EASY", label: "Easy", color: "#3b82f6" },
];

const countFormatter = new Intl.NumberFormat("en-US");

/**
 * Formats a YYYY-MM-DD local date without shifting it to the
 * browser's time zone.
 */
function formatChartDate(date: string): string {
  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
    timeZone: "UTC",
  }).format(new Date(`${date}T12:00:00Z`));
}

interface DashboardMetricProps {
  title: string;
  value: number;
  description: string;
  icon: LucideIcon;
}

function DashboardMetric({
  title,
  value,
  description,
  icon: Icon,
}: DashboardMetricProps) {
  return (
    <article className="rounded-2xl border border-border bg-card p-5 text-card-foreground shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <p className="text-sm font-medium text-muted-foreground">{title}</p>

        <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-pink-100 text-pink-500 dark:bg-pink-950/40 dark:text-pink-400">
          <Icon className="h-5 w-5" aria-hidden="true" />
        </span>
      </div>

      <p className="mt-3 text-3xl font-bold tracking-tight tabular-nums">
        {countFormatter.format(value)}
      </p>

      <p className="mt-1 text-xs text-muted-foreground">{description}</p>
    </article>
  );
}

function DashboardPanel({
  title,
  description,
  children,
}: {
  title: string;
  description: string;
  children: ReactNode;
}) {
  return (
    <section className="min-w-0 rounded-2xl border border-border bg-card p-5 text-card-foreground shadow-sm sm:p-6">
      <h2 className="text-lg font-semibold">{title}</h2>

      <p className="mt-1 text-sm text-muted-foreground">{description}</p>

      <div className="mt-6">{children}</div>
    </section>
  );
}

function EmptyChart({ message }: { message: string }) {
  return (
    <div className="flex h-60 items-center justify-center rounded-xl border border-dashed border-border bg-muted/20 px-4 text-center text-sm text-muted-foreground">
      {message}
    </div>
  );
}

/**
 * Displays aggregated FSRS learning statistics for the
 * authenticated user.
 */
export default function ReviewDashboardClient() {
  const { token, isAuthLoading } = useAuth();

  const [state, setState] = useState<DashboardState>({
    status: "loading",
  });

  const [reloadCounter, setReloadCounter] = useState(0);

  useEffect(() => {
    if (isAuthLoading || !token) {
      return;
    }

    const abortController = new AbortController();

    async function loadDashboard(authToken: string) {
      try {
        const data = await getReviewDashboard(
          authToken,
          abortController.signal,
        );

        if (!abortController.signal.aborted) {
          setState({
            status: "ready",
            token: authToken,
            reloadCounter,
            data,
          });
        }
      } catch (error: unknown) {
        if (!abortController.signal.aborted) {
          setState({
            status: "error",
            token: authToken,
            reloadCounter,
            message:
              error instanceof Error
                ? error.message
                : "Failed to load spaced repetition statistics.",
          });
        }
      }
    }

    void loadDashboard(token);

    return () => abortController.abort();
  }, [token, isAuthLoading, reloadCounter]);

  if (isAuthLoading) {
    return <DashboardLoading />;
  }

  if (!token) {
    return (
      <main className="min-h-[calc(100svh-4rem)] bg-background px-4 py-12 text-foreground">
        <div className="mx-auto max-w-lg rounded-3xl border border-border bg-card p-8 text-center shadow-sm">
          <BrainCircuit
            className="mx-auto h-10 w-10 text-pink-500"
            aria-hidden="true"
          />

          <h1 className="mt-4 text-2xl font-semibold">
            Spaced repetition statistics
          </h1>

          <p className="mt-3 text-muted-foreground">
            Log in to view your learning progress and upcoming reviews.
          </p>

          <Button
            asChild
            className="mt-6 bg-pink-500 text-white hover:bg-pink-600"
          >
            <Link href="/login">Log in</Link>
          </Button>
        </div>
      </main>
    );
  }

  if (
    state.status === "loading" ||
    state.token !== token ||
    state.reloadCounter !== reloadCounter
  ) {
    return <DashboardLoading />;
  }

  if (state.status === "error") {
    return (
      <main className="min-h-[calc(100svh-4rem)] bg-background px-4 py-12 text-foreground">
        <div className="mx-auto max-w-xl rounded-3xl border border-destructive/30 bg-card p-8 shadow-sm">
          <AlertCircle
            className="h-8 w-8 text-destructive"
            aria-hidden="true"
          />

          <h1 className="mt-4 text-xl font-semibold">
            Could not load FSRS statistics
          </h1>

          <p className="mt-2 text-sm text-destructive" role="alert">
            {state.message}
          </p>

          <Button
            type="button"
            onClick={() => setReloadCounter((value) => value + 1)}
            className="mt-5 bg-pink-500 text-white hover:bg-pink-600"
          >
            <RefreshCw className="h-4 w-4" aria-hidden="true" />
            Try again
          </Button>
        </div>
      </main>
    );
  }

  const dashboard = state.data;

  const activityTotal = dashboard.activity.reduce(
    (sum, day) => sum + day.reviews,
    0,
  );

  const ratingTotal = dashboard.ratings.reduce(
    (sum, item) => sum + item.count,
    0,
  );

  const forecastTotal = dashboard.forecast.reduce(
    (sum, day) => sum + day.dueCards,
    0,
  );

  return (
    <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
      <div className="mx-auto max-w-7xl space-y-6 px-4 py-8 sm:px-6 lg:px-8">
        <header className="overflow-hidden rounded-3xl border border-border bg-card shadow-sm">
          <div className="bg-pink-50/70 p-6 dark:bg-pink-950/20 sm:p-8">
            <div className="mb-3 inline-flex items-center gap-2 rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-600 dark:bg-pink-950/60 dark:text-pink-400">
              <BrainCircuit className="h-4 w-4" aria-hidden="true" />
              FSRS learning insights
            </div>

            <div className="flex flex-wrap items-end justify-between gap-5">
              <div>
                <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">
                  Spaced repetition
                </h1>

                <p className="mt-2 max-w-2xl text-sm text-muted-foreground sm:text-base">
                  Track your learning progress, review history, and upcoming
                  flashcards across all your sets.
                </p>

                <p className="mt-2 text-xs text-muted-foreground">
                  Daily statistics use {dashboard.timezone}.
                </p>
              </div>

              <div className="flex flex-wrap gap-2">
                <Button
                  asChild
                  variant="outline"
                  className="border-border bg-card"
                >
                  <Link href="/profile/stats">
                    <ArrowLeft className="h-4 w-4" aria-hidden="true" />
                    My stats
                  </Link>
                </Button>

                <Button
                  asChild
                  variant="outline"
                  className="border-border bg-card"
                >
                  <Link href="/profile/settings">
                    <Settings2 className="h-4 w-4" aria-hidden="true" />
                    Settings
                  </Link>
                </Button>

                <Button
                  type="button"
                  variant="outline"
                  className="border-border bg-card"
                  onClick={() => setReloadCounter((value) => value + 1)}
                >
                  <RefreshCw className="h-4 w-4" aria-hidden="true" />
                  Refresh
                </Button>
              </div>
            </div>
          </div>
        </header>

        <section
          aria-label="Learning overview"
          className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4"
        >
          <DashboardMetric
            title="Due now"
            value={dashboard.dueNow}
            description="Includes overdue cards"
            icon={Clock3}
          />

          <DashboardMetric
            title="Reviewed today"
            value={dashboard.reviewedToday}
            description="Ratings submitted today"
            icon={CheckCircle2}
          />

          <DashboardMetric
            title="Total cards"
            value={dashboard.totalCards}
            description="Across all your flashcard sets"
            icon={Layers3}
          />

          <DashboardMetric
            title="New cards"
            value={dashboard.newCards}
            description="Not introduced to FSRS yet"
            icon={Sparkles}
          />
        </section>

        {dashboard.totalCards === 0 && (
          <section className="flex flex-col gap-4 rounded-2xl border border-dashed border-pink-200 bg-pink-50/50 p-5 dark:border-pink-900 dark:bg-pink-950/20 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h2 className="font-semibold">No flashcards yet</h2>

              <p className="mt-1 text-sm text-muted-foreground">
                Create your first set to start tracking FSRS learning
                statistics.
              </p>
            </div>

            <Button
              asChild
              className="shrink-0 bg-pink-500 text-white hover:bg-pink-600"
            >
              <Link href="/create-set">
                <BookOpen className="h-4 w-4" aria-hidden="true" />
                Create a set
              </Link>
            </Button>
          </section>
        )}

        <div className="grid gap-5 lg:grid-cols-2">
          <DashboardPanel
            title="Review activity"
            description="Ratings submitted during the last 14 local days"
          >
            {activityTotal === 0 ? (
              <EmptyChart message="No review activity yet. Your daily progress will appear here." />
            ) : (
              <div
                className="h-64 w-full"
                role="img"
                aria-label="Bar chart of daily reviews for the last 14 days"
              >
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart
                    data={dashboard.activity}
                    margin={{
                      top: 8,
                      right: 6,
                      left: -16,
                      bottom: 2,
                    }}
                  >
                    <CartesianGrid
                      vertical={false}
                      stroke="var(--border)"
                      strokeDasharray="3 3"
                    />

                    <XAxis
                      dataKey="date"
                      tickFormatter={formatChartDate}
                      tick={{
                        fill: "var(--muted-foreground)",
                        fontSize: 11,
                      }}
                      tickLine={false}
                      axisLine={false}
                      minTickGap={12}
                    />

                    <YAxis
                      allowDecimals={false}
                      tick={{
                        fill: "var(--muted-foreground)",
                        fontSize: 11,
                      }}
                      tickLine={false}
                      axisLine={false}
                    />

                    <Tooltip
                      labelFormatter={(date) => formatChartDate(String(date))}
                      contentStyle={{
                        backgroundColor: "var(--popover)",
                        border: "1px solid var(--border)",
                        borderRadius: 12,
                        color: "var(--popover-foreground)",
                      }}
                    />

                    <Bar
                      name="Reviews"
                      dataKey="reviews"
                      fill="#ec4899"
                      radius={[5, 5, 0, 0]}
                      maxBarSize={26}
                    />

                    <Bar
                      name="New cards"
                      dataKey="newCards"
                      fill="#f59e0b"
                      radius={[5, 5, 0, 0]}
                      maxBarSize={26}
                    />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}

            <p className="mt-3 text-xs text-muted-foreground">
              {countFormatter.format(activityTotal)} ratings in the last 14
              days.
            </p>
          </DashboardPanel>

          <DashboardPanel
            title="Upcoming reviews"
            description="Today includes overdue cards and reviews due later today"
          >
            {forecastTotal === 0 ? (
              <EmptyChart message="No scheduled reviews in the next seven days." />
            ) : (
              <div
                className="h-64 w-full"
                role="img"
                aria-label="Bar chart of scheduled reviews for the next seven days"
              >
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart
                    data={dashboard.forecast}
                    margin={{
                      top: 8,
                      right: 6,
                      left: -16,
                      bottom: 2,
                    }}
                  >
                    <CartesianGrid
                      vertical={false}
                      stroke="var(--border)"
                      strokeDasharray="3 3"
                    />

                    <XAxis
                      dataKey="date"
                      tickFormatter={formatChartDate}
                      tick={{
                        fill: "var(--muted-foreground)",
                        fontSize: 11,
                      }}
                      tickLine={false}
                      axisLine={false}
                      minTickGap={12}
                    />

                    <YAxis
                      allowDecimals={false}
                      tick={{
                        fill: "var(--muted-foreground)",
                        fontSize: 11,
                      }}
                      tickLine={false}
                      axisLine={false}
                    />

                    <Tooltip
                      labelFormatter={(date) => formatChartDate(String(date))}
                      contentStyle={{
                        backgroundColor: "var(--popover)",
                        border: "1px solid var(--border)",
                        borderRadius: 12,
                        color: "var(--popover-foreground)",
                      }}
                    />

                    <Bar
                      name="Due cards"
                      dataKey="dueCards"
                      fill="#a855f7"
                      radius={[5, 5, 0, 0]}
                      maxBarSize={48}
                    />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}

            <p className="mt-3 text-xs text-muted-foreground">
              Forecast based on current FSRS schedules. Actual dates may change
              after reviews.
            </p>
          </DashboardPanel>
        </div>

        <div className="grid gap-5 lg:grid-cols-2">
          <DashboardPanel
            title="Learning stages"
            description="Current FSRS stage of every flashcard"
          >
            <div
              className="flex h-3 overflow-hidden rounded-full bg-muted"
              role="img"
              aria-label="Proportion of flashcards in each learning stage"
            >
              {STAGES.map((stage) => (
                <div
                  key={stage.key}
                  style={{
                    width: `${
                      dashboard.totalCards > 0
                        ? (dashboard[stage.key] / dashboard.totalCards) * 100
                        : 0
                    }%`,
                    backgroundColor: stage.color,
                  }}
                />
              ))}
            </div>

            <div className="mt-5 space-y-4">
              {STAGES.map((stage) => (
                <div
                  key={stage.key}
                  className="flex items-center justify-between gap-4"
                >
                  <div className="flex min-w-0 items-center gap-3">
                    <span
                      className="h-3 w-3 shrink-0 rounded-full"
                      style={{ backgroundColor: stage.color }}
                      aria-hidden="true"
                    />

                    <div>
                      <p className="text-sm font-medium">{stage.label}</p>

                      <p className="text-xs text-muted-foreground">
                        {stage.description}
                      </p>
                    </div>
                  </div>

                  <span className="font-semibold tabular-nums">
                    {countFormatter.format(dashboard[stage.key])}
                  </span>
                </div>
              ))}
            </div>
          </DashboardPanel>

          <DashboardPanel
            title="Review ratings"
            description="Again, Hard, Good and Easy — last 14 local days"
          >
            {ratingTotal === 0 ? (
              <EmptyChart message="No ratings yet. Review flashcards to see your rating breakdown." />
            ) : (
              <div className="space-y-5">
                {RATINGS.map(({ rating, label, color }) => {
                  const count =
                    dashboard.ratings.find((item) => item.rating === rating)
                      ?.count ?? 0;

                  const percentage = (count / ratingTotal) * 100;

                  return (
                    <div key={rating}>
                      <div className="mb-2 flex items-center justify-between gap-3 text-sm">
                        <span className="flex items-center gap-2 font-medium">
                          <span
                            className="h-2.5 w-2.5 rounded-full"
                            style={{ backgroundColor: color }}
                            aria-hidden="true"
                          />
                          {label}
                        </span>

                        <span className="text-muted-foreground tabular-nums">
                          {countFormatter.format(count)}{" "}
                          <span className="text-xs">
                            ({Math.round(percentage)}%)
                          </span>
                        </span>
                      </div>

                      <div className="h-2 overflow-hidden rounded-full bg-muted">
                        <div
                          className="h-full rounded-full transition-[width]"
                          style={{
                            width: `${percentage}%`,
                            backgroundColor: color,
                          }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </DashboardPanel>
        </div>

        <div className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-border bg-card p-5 shadow-sm">
          <div>
            <h2 className="font-semibold">Ready for another review session?</h2>

            <p className="mt-1 text-sm text-muted-foreground">
              Choose one of your flashcard sets to continue learning.
            </p>
          </div>

          <Button asChild className="bg-pink-500 text-white hover:bg-pink-600">
            <Link href="/profile/sets">
              <BookOpen className="h-4 w-4" aria-hidden="true" />
              My flashcard sets
            </Link>
          </Button>
        </div>
      </div>
    </main>
  );
}

function DashboardLoading() {
  return (
    <main className="min-h-[calc(100svh-4rem)] bg-background px-4 py-16 text-foreground">
      <div
        role="status"
        className="mx-auto flex max-w-7xl items-center justify-center gap-3 text-sm text-muted-foreground"
      >
        <Loader2
          className="h-5 w-5 animate-spin text-pink-500"
          aria-hidden="true"
        />
        Loading spaced repetition statistics...
      </div>
    </main>
  );
}
