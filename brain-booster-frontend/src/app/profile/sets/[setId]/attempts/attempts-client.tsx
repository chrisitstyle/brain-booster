"use client";

import Link from "next/link";
import {
  AlertCircle,
  ArrowLeft,
  BarChart3,
  ClipboardList,
  Clock3,
  Eye,
  Loader2,
} from "lucide-react";

import { useAuth } from "@/context/AuthContext";
import { useGameProgress } from "@/hooks/game-results";

interface AttemptsClientProps {
  setId: number;
}

function formatDateTime(value: string) {
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

function formatPercentage(value: number) {
  const normalizedValue = Number.isFinite(value) ? value : 0;

  const clampedValue = Math.min(Math.max(normalizedValue, 0), 100);

  return `${Math.round(clampedValue)}%`;
}

export default function AttemptsClient({ setId }: AttemptsClientProps) {
  const { token, isAuthenticated, isAuthLoading } = useAuth();

  const { data: attempts, isLoading, error } = useGameProgress(setId, token);

  const orderedAttempts = [...(attempts ?? [])].sort(
    (firstAttempt, secondAttempt) =>
      new Date(secondAttempt.completedAt).getTime() -
      new Date(firstAttempt.completedAt).getTime(),
  );

  if (isAuthLoading) {
    return (
      <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
        <div className="mx-auto max-w-6xl px-4 py-10">
          <div
            className="flex items-center gap-3 rounded-xl border border-dashed border-border bg-card px-4 py-3 text-sm text-muted-foreground"
            role="status"
          >
            <Loader2
              className="h-4 w-4 animate-spin text-pink-500 dark:text-pink-400"
              aria-hidden="true"
            />

            <span>Loading attempts...</span>
          </div>
        </div>
      </main>
    );
  }

  if (!token || !isAuthenticated) {
    return (
      <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
        <div className="mx-auto max-w-6xl px-4 py-10">
          <div
            className="flex items-start gap-3 rounded-xl border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive"
            role="alert"
          >
            <AlertCircle
              className="mt-0.5 h-4 w-4 shrink-0"
              aria-hidden="true"
            />

            <span>You need to be logged in to view attempts.</span>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
      <div className="mx-auto max-w-6xl space-y-6 px-4 py-8">
        <Link
          href={`/profile/sets/${setId}/stats`}
          className="inline-flex items-center gap-2 rounded-sm text-sm font-medium text-muted-foreground transition-colors hover:text-pink-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring dark:hover:text-pink-400"
        >
          <ArrowLeft size={16} aria-hidden="true" />
          Back to stats
        </Link>

        <section className="overflow-hidden rounded-3xl border border-border bg-card text-card-foreground shadow-sm">
          <div className="bg-pink-50/70 p-6 dark:bg-pink-950/20 sm:p-8">
            <p className="mb-3 inline-flex items-center gap-2 rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
              <ClipboardList size={14} aria-hidden="true" />
              Attempt history
            </p>

            <h1 className="text-3xl font-bold tracking-tight text-card-foreground">
              Game attempts
            </h1>

            <p className="mt-3 text-muted-foreground">
              Review every completed attempt for this flashcard set.
            </p>
          </div>
        </section>

        {isLoading && (
          <div
            className="flex items-center gap-3 rounded-xl border border-dashed border-border bg-card px-4 py-3 text-sm text-muted-foreground"
            role="status"
          >
            <Loader2
              className="h-4 w-4 animate-spin text-pink-500 dark:text-pink-400"
              aria-hidden="true"
            />

            <span>Loading attempts...</span>
          </div>
        )}

        {error && (
          <div
            className="flex items-start gap-3 rounded-xl border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive"
            role="alert"
          >
            <AlertCircle
              className="mt-0.5 h-4 w-4 shrink-0"
              aria-hidden="true"
            />

            <span>{error}</span>
          </div>
        )}

        {!isLoading && !error && orderedAttempts.length === 0 && (
          <section className="rounded-3xl border border-dashed border-border bg-card p-10 text-center text-card-foreground shadow-sm">
            <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-pink-100 text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
              <ClipboardList size={28} aria-hidden="true" />
            </div>

            <h2 className="mt-4 text-xl font-semibold text-card-foreground">
              No attempts yet
            </h2>

            <p className="mt-2 text-sm text-muted-foreground">
              Complete a game to see it in your attempt history.
            </p>
          </section>
        )}

        {!isLoading && !error && orderedAttempts.length > 0 && (
          <div className="overflow-hidden rounded-3xl border border-border bg-card text-card-foreground shadow-sm">
            <div className="overflow-x-auto">
              <table className="w-full min-w-[850px] text-sm">
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
                  {orderedAttempts.map((attempt) => (
                    <tr
                      key={attempt.attemptId}
                      className="border-t border-border transition-colors hover:bg-pink-50/40 dark:hover:bg-pink-950/20"
                    >
                      <td className="px-5 py-4 text-muted-foreground">
                        <span className="inline-flex items-center gap-2">
                          <Clock3 size={15} aria-hidden="true" />

                          {formatDateTime(attempt.completedAt)}
                        </span>
                      </td>

                      <td className="px-5 py-4">
                        <span className="rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
                          {attempt.mode}
                        </span>
                      </td>

                      <td className="px-5 py-4 font-medium text-card-foreground">
                        {attempt.score}/{attempt.totalQuestions}
                      </td>

                      <td className="px-5 py-4">
                        <span className="inline-flex items-center gap-2 font-medium text-card-foreground">
                          <BarChart3
                            size={15}
                            className="text-pink-500 dark:text-pink-400"
                            aria-hidden="true"
                          />

                          {formatPercentage(attempt.percentage)}
                        </span>
                      </td>

                      <td className="px-5 py-4 text-muted-foreground">
                        {formatDuration(attempt.durationSeconds)}
                      </td>

                      <td className="px-5 py-4">
                        <Link
                          href={`/profile/sets/${setId}/attempts/${attempt.attemptId}`}
                          className="inline-flex items-center gap-2 rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500 transition-colors hover:bg-pink-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring dark:bg-pink-950/50 dark:text-pink-400 dark:hover:bg-pink-900/60"
                        >
                          <Eye size={14} aria-hidden="true" />
                          View details
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </main>
  );
}

function TableHeader({ children }: { children: React.ReactNode }) {
  return (
    <th
      scope="col"
      className="px-5 py-4 text-left font-semibold text-foreground"
    >
      {children}
    </th>
  );
}
