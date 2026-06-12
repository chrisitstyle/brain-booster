"use client";

import Link from "next/link";
import {
  BarChart3,
  BookOpen,
  Brain,
  ClipboardList,
  TrendingUp,
} from "lucide-react";

import { useAuth } from "@/context/AuthContext";
import {
  getSetDisplayName,
  useUserFlashcardSets,
} from "@/hooks/use-user-flashcard-sets";

export default function MyStatsClient() {
  const { token, isAuthenticated, isAuthLoading } = useAuth();

  const { data: flashcardSets, isLoading, error } = useUserFlashcardSets(token);

  if (isAuthLoading) {
    return (
      <main className="min-h-[calc(100vh-4rem)] bg-gray-50">
        <div className="mx-auto max-w-6xl px-4 py-10">
          <p className="rounded-xl border border-dashed border-gray-200 bg-white px-4 py-3 text-sm text-slate-500">
            Loading statistics...
          </p>
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
                <TrendingUp size={14} />
                Brain Booster Stats
              </p>

              <h1 className="text-3xl font-bold tracking-tight text-slate-950">
                My statistics
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
              <TrendingUp size={14} />
              Brain Booster Stats
            </p>

            <h1 className="text-3xl font-bold tracking-tight text-slate-950 sm:text-4xl">
              My statistics
            </h1>

            <p className="mt-3 max-w-2xl text-slate-500">
              Choose a flashcard set to view your progress, attempts, weak
              flashcards, and question type accuracy.
            </p>
          </div>
        </section>

        {isLoading ? (
          <p className="rounded-xl border border-dashed border-gray-200 bg-white px-4 py-3 text-sm text-slate-500">
            Loading flashcard sets...
          </p>
        ) : null}

        {error ? (
          <p className="rounded-xl border border-pink-200 bg-pink-50 px-4 py-3 text-sm text-pink-600">
            {error}
          </p>
        ) : null}

        {!isLoading && !error && flashcardSets.length === 0 ? (
          <section className="rounded-3xl border border-dashed border-gray-200 bg-white p-10 text-center shadow-sm">
            <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-pink-100 text-pink-500">
              <Brain size={28} />
            </div>

            <h2 className="mt-4 text-xl font-semibold text-slate-950">
              No flashcard sets
            </h2>

            <p className="mx-auto mt-2 max-w-md text-sm text-slate-500">
              Create a flashcard set and complete a game to start collecting
              learning statistics.
            </p>

            <Link
              href="/profile/sets"
              className="mt-5 inline-flex items-center gap-2 rounded-xl bg-pink-500 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-pink-600"
            >
              <BookOpen size={16} />
              View my sets
            </Link>
          </section>
        ) : null}

        {flashcardSets.length > 0 ? (
          <section className="space-y-4">
            <div className="flex items-start gap-3">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-pink-100 text-pink-500">
                <BookOpen size={20} />
              </div>

              <div>
                <h2 className="text-lg font-semibold tracking-tight text-slate-950">
                  Choose learning material
                </h2>

                <p className="mt-1 text-sm text-slate-500">
                  Select a set to open its detailed statistics.
                </p>
              </div>
            </div>

            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {flashcardSets.map((flashcardSet) => (
                <article
                  key={flashcardSet.setId}
                  className="overflow-hidden rounded-3xl border border-gray-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:border-pink-200 hover:shadow-md"
                >
                  <div className="h-1 bg-pink-500" />

                  <div className="p-5">
                    <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-pink-100 text-pink-500">
                      <BookOpen size={20} />
                    </div>

                    <h3 className="mt-4 text-lg font-semibold text-slate-950">
                      {getSetDisplayName(flashcardSet)}
                    </h3>

                    <p className="mt-2 text-sm text-slate-500">
                      View progress, attempt history and weak flashcards for
                      this set.
                    </p>

                    <div className="mt-5 flex flex-wrap gap-2">
                      <Link
                        href={`/profile/sets/${flashcardSet.setId}/stats`}
                        className="inline-flex items-center gap-2 rounded-xl bg-pink-500 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-pink-600"
                      >
                        <BarChart3 size={16} />
                        View statistics
                      </Link>

                      <Link
                        href={`/profile/sets/${flashcardSet.setId}/attempts`}
                        className="inline-flex items-center gap-2 rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm font-semibold text-slate-600 transition hover:border-pink-200 hover:text-pink-500"
                      >
                        <ClipboardList size={16} />
                        Attempts
                      </Link>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          </section>
        ) : null}
      </div>
    </main>
  );
}
