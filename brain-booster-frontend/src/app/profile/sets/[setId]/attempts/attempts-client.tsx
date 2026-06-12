"use client";

import Link from "next/link";
import { ArrowLeft, BarChart3, ClipboardList, Clock3, Eye } from "lucide-react";

import { useAuth } from "@/context/AuthContext";
import { useGameProgress } from "@/hooks/game-results";

interface AttemptsClientProps {
  setId: number;
}

function formatDateTime(value: string) {
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

function formatPercentage(value: number) {
  return `${Math.round(value)}%`;
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
      <main className="min-h-[calc(100vh-4rem)] bg-gray-50">
        <div className="mx-auto max-w-6xl px-4 py-10 text-sm text-slate-500">
          Loading attempts...
        </div>
      </main>
    );
  }

  if (!token || !isAuthenticated) {
    return (
      <main className="min-h-[calc(100vh-4rem)] bg-gray-50">
        <div className="mx-auto max-w-6xl px-4 py-10">
          <p className="rounded-xl border border-pink-200 bg-pink-50 px-4 py-3 text-sm text-pink-600">
            You need to be logged in to view attempts.
          </p>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-[calc(100vh-4rem)] bg-gray-50">
      <div className="mx-auto max-w-6xl space-y-6 px-4 py-8">
        <Link
          href={`/profile/sets/${setId}/stats`}
          className="inline-flex items-center gap-2 text-sm font-medium text-slate-500 transition hover:text-pink-500"
        >
          <ArrowLeft size={16} />
          Back to stats
        </Link>

        <section className="overflow-hidden rounded-3xl border border-gray-200 bg-white shadow-sm">
          <div className="bg-pink-50/70 p-6 sm:p-8">
            <p className="mb-3 inline-flex items-center gap-2 rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500">
              <ClipboardList size={14} />
              Attempt history
            </p>

            <h1 className="text-3xl font-bold tracking-tight text-slate-950">
              Game attempts
            </h1>

            <p className="mt-3 text-slate-500">
              Review every completed attempt for this flashcard set.
            </p>
          </div>
        </section>

        {isLoading ? (
          <p className="rounded-xl border border-dashed border-gray-200 bg-white px-4 py-3 text-sm text-slate-500">
            Loading attempts...
          </p>
        ) : null}

        {error ? (
          <p className="rounded-xl border border-pink-200 bg-pink-50 px-4 py-3 text-sm text-pink-600">
            {error}
          </p>
        ) : null}

        {!isLoading && !error && orderedAttempts.length === 0 ? (
          <section className="rounded-3xl border border-dashed border-gray-200 bg-white p-10 text-center shadow-sm">
            <ClipboardList size={32} className="mx-auto text-pink-500" />

            <h2 className="mt-4 text-xl font-semibold text-slate-950">
              No attempts yet
            </h2>

            <p className="mt-2 text-sm text-slate-500">
              Complete a game to see it in your attempt history.
            </p>
          </section>
        ) : null}

        {orderedAttempts.length > 0 ? (
          <div className="overflow-hidden rounded-3xl border border-gray-200 bg-white shadow-sm">
            <div className="overflow-x-auto">
              <table className="w-full min-w-[850px] text-sm">
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
                  {orderedAttempts.map((attempt) => (
                    <tr
                      key={attempt.attemptId}
                      className="border-t border-gray-200 transition hover:bg-pink-50/40"
                    >
                      <td className="px-5 py-4 text-slate-500">
                        <span className="inline-flex items-center gap-2">
                          <Clock3 size={15} />
                          {formatDateTime(attempt.completedAt)}
                        </span>
                      </td>

                      <td className="px-5 py-4">
                        <span className="rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500">
                          {attempt.mode}
                        </span>
                      </td>

                      <td className="px-5 py-4 font-medium text-slate-950">
                        {attempt.score}/{attempt.totalQuestions}
                      </td>

                      <td className="px-5 py-4">
                        <span className="inline-flex items-center gap-2 font-medium text-slate-950">
                          <BarChart3 size={15} className="text-pink-500" />
                          {formatPercentage(attempt.percentage)}
                        </span>
                      </td>

                      <td className="px-5 py-4 text-slate-500">
                        {formatDuration(attempt.durationSeconds)}
                      </td>

                      <td className="px-5 py-4">
                        <Link
                          href={`/profile/sets/${setId}/attempts/${attempt.attemptId}`}
                          className="inline-flex items-center gap-2 rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500 transition hover:bg-pink-200"
                        >
                          <Eye size={14} />
                          View details
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        ) : null}
      </div>
    </main>
  );
}
