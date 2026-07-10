"use client";

import Link from "next/link";
import {
  AlertCircle,
  BarChart3,
  BookOpen,
  Brain,
  ClipboardList,
  Loader2,
  TrendingUp,
} from "lucide-react";

import { Button } from "@/components/ui/button";
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

            <span>Loading statistics...</span>
          </div>
        </div>
      </main>
    );
  }

  if (!token || !isAuthenticated) {
    return (
      <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
        <div className="mx-auto max-w-6xl px-4 py-10">
          <section className="overflow-hidden rounded-3xl border border-border bg-card text-card-foreground shadow-sm">
            <div className="bg-pink-50/70 p-8 dark:bg-pink-950/20">
              <p className="mb-3 inline-flex items-center gap-2 rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
                <TrendingUp size={14} aria-hidden="true" />
                Brain Booster Stats
              </p>

              <h1 className="text-3xl font-bold tracking-tight text-card-foreground">
                My statistics
              </h1>

              <p className="mt-3 max-w-2xl text-muted-foreground">
                You need to be logged in to view your learning statistics.
              </p>

              <Button
                asChild
                className="mt-6 bg-pink-500 text-white hover:bg-pink-600"
              >
                <Link href="/login">Log in</Link>
              </Button>
            </div>
          </section>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
      <div className="mx-auto max-w-6xl space-y-8 px-4 py-8">
        <section className="overflow-hidden rounded-3xl border border-border bg-card text-card-foreground shadow-sm">
          <div className="bg-pink-50/70 p-6 dark:bg-pink-950/20 sm:p-8">
            <p className="mb-3 inline-flex items-center gap-2 rounded-full bg-pink-100 px-3 py-1 text-xs font-semibold text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
              <TrendingUp size={14} aria-hidden="true" />
              Brain Booster Stats
            </p>

            <h1 className="text-3xl font-bold tracking-tight text-card-foreground sm:text-4xl">
              My statistics
            </h1>

            <p className="mt-3 max-w-2xl text-muted-foreground">
              Choose a flashcard set to view your progress, attempts, weak
              flashcards, and question type accuracy.
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

            <span>Loading flashcard sets...</span>
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

        {!isLoading && !error && flashcardSets.length === 0 && (
          <section className="rounded-3xl border border-dashed border-border bg-card p-10 text-center text-card-foreground shadow-sm">
            <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-pink-100 text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
              <Brain size={28} aria-hidden="true" />
            </div>

            <h2 className="mt-4 text-xl font-semibold text-card-foreground">
              No flashcard sets
            </h2>

            <p className="mx-auto mt-2 max-w-md text-sm text-muted-foreground">
              Create a flashcard set and complete a game to start collecting
              learning statistics.
            </p>

            <Button
              asChild
              className="mt-5 bg-pink-500 text-white hover:bg-pink-600"
            >
              <Link href="/profile/sets">
                <BookOpen className="mr-2 h-4 w-4" aria-hidden="true" />
                View my sets
              </Link>
            </Button>
          </section>
        )}

        {!isLoading && !error && flashcardSets.length > 0 && (
          <section
            className="space-y-4"
            aria-labelledby="learning-material-heading"
          >
            <div className="flex items-start gap-3">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-pink-100 text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
                <BookOpen size={20} aria-hidden="true" />
              </div>

              <div>
                <h2
                  id="learning-material-heading"
                  className="text-lg font-semibold tracking-tight text-foreground"
                >
                  Choose learning material
                </h2>

                <p className="mt-1 text-sm text-muted-foreground">
                  Select a set to open its detailed statistics.
                </p>
              </div>
            </div>

            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {flashcardSets.map((flashcardSet) => (
                <article
                  key={flashcardSet.setId}
                  className="overflow-hidden rounded-3xl border border-border bg-card text-card-foreground shadow-sm transition-all hover:-translate-y-0.5 hover:border-pink-200 hover:shadow-md dark:hover:border-pink-900"
                >
                  <div className="h-1 bg-pink-500" />

                  <div className="p-5">
                    <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-pink-100 text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
                      <BookOpen size={20} aria-hidden="true" />
                    </div>

                    <h3 className="mt-4 break-words text-lg font-semibold text-card-foreground">
                      {getSetDisplayName(flashcardSet)}
                    </h3>

                    <p className="mt-2 text-sm text-muted-foreground">
                      View progress, attempt history and weak flashcards for
                      this set.
                    </p>

                    <div className="mt-5 flex flex-wrap gap-2">
                      <Button
                        asChild
                        className="bg-pink-500 text-white hover:bg-pink-600"
                      >
                        <Link
                          href={`/profile/sets/${flashcardSet.setId}/stats`}
                        >
                          <BarChart3
                            className="mr-2 h-4 w-4"
                            aria-hidden="true"
                          />
                          View statistics
                        </Link>
                      </Button>

                      <Button
                        asChild
                        variant="outline"
                        className="border-border bg-background text-muted-foreground hover:border-pink-200 hover:bg-pink-50 hover:text-pink-500 dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-400"
                      >
                        <Link
                          href={`/profile/sets/${flashcardSet.setId}/attempts`}
                        >
                          <ClipboardList
                            className="mr-2 h-4 w-4"
                            aria-hidden="true"
                          />
                          Attempts
                        </Link>
                      </Button>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          </section>
        )}
      </div>
    </main>
  );
}
