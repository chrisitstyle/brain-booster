import Link from "next/link";
import { HomeButton } from "@/components/HomeButton";

const FEATURES = [
  {
    title: "Flashcards",
    description:
      "Create your own or study from millions of flashcard sets created by other students.",
  },
  {
    title: "Learn Mode",
    description:
      "Master concepts through adaptive learning that identifies what you need to study.",
  },
  {
    title: "Test Yourself",
    description:
      "Practice tests with different question types to ensure you're prepared for exams.",
  },
];

export default function Home() {
  return (
    <div className="flex min-h-screen flex-col bg-background text-foreground">
      <main className="flex-1">
        <section className="container mx-auto px-4 py-12 md:py-16">
          <div className="mx-auto max-w-4xl text-center">
            <h1 className="mb-6 text-4xl font-bold tracking-tight text-foreground md:text-5xl">
              Learn it. Master it.{" "}
              <span className="text-pink-500">Boost it.</span>
            </h1>

            <p className="mb-8 text-xl text-muted-foreground">
              Join over 60 million students using BrainBooster&apos;s
              science-backed flashcards, practice tests and expert solutions to
              improve their grades and reach their goals.
            </p>

            <div className="flex flex-col items-center justify-center gap-4 sm:flex-row">
              <HomeButton className="w-full bg-pink-500 text-white hover:bg-pink-600 sm:w-auto">
                <Link href="/signup">Sign up free</Link>
              </HomeButton>

              <HomeButton
                variant="outline"
                className="w-full border-pink-200 bg-background text-pink-500 hover:bg-pink-50 hover:text-pink-600 dark:border-pink-900 dark:hover:bg-pink-950/40 dark:hover:text-pink-400 sm:w-auto"
              >
                Find your textbook
              </HomeButton>
            </div>
          </div>

          <div className="mt-16 grid gap-8 md:grid-cols-3">
            {FEATURES.map((feature) => (
              <article
                key={feature.title}
                className="rounded-xl border border-border bg-card p-6 text-card-foreground shadow-sm transition-all duration-200 hover:-translate-y-1 hover:border-pink-200 hover:shadow-md dark:hover:border-pink-900"
              >
                <h2 className="mb-3 text-xl font-semibold text-card-foreground">
                  {feature.title}
                </h2>

                <p className="text-muted-foreground">{feature.description}</p>
              </article>
            ))}
          </div>
        </section>
      </main>
    </div>
  );
}
