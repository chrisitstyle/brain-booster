"use client";
import type React from "react";
import { cn } from "@/lib/utils";
import Navbar from "@/components/Navbar";

export default function Home() {
  return (
    <div className="flex min-h-screen flex-col">
      <Navbar />
      <main className="flex-1">
        <section className="container mx-auto px-4 py-12">
          <div className="mx-auto max-w-4xl text-center">
            <h1 className="mb-6 text-4xl font-bold text-gray-800 md:text-5xl">
              Learn it. Master it.{" "}
              <span className="text-pink-500">Boost it.</span>
            </h1>
            <p className="mb-8 text-xl text-gray-600">
              Join over 60 million students using BrainBooster&apos;s
              science-backed flashcards, practice tests and expert solutions to
              improve their grades and reach their goals.
            </p>
            <div className="flex flex-col items-center justify-center gap-4 sm:flex-row">
              <Button className="w-full rounded-full bg-pink-500 px-8 py-6 text-lg font-medium text-white hover:bg-pink-600 sm:w-auto">
                Sign up free
              </Button>
              <Button
                variant="outline"
                className="w-full rounded-full border-pink-200 px-8 py-6 text-lg font-medium text-pink-500 hover:bg-pink-50 sm:w-auto"
              >
                Find your textbook
              </Button>
            </div>
          </div>

          <div className="mt-16 grid gap-8 md:grid-cols-3">
            {[
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
            ].map((feature, index) => (
              <div
                key={index}
                className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm transition-all hover:shadow-md"
              >
                <h3 className="mb-3 text-xl font-semibold text-gray-800">
                  {feature.title}
                </h3>
                <p className="text-gray-600">{feature.description}</p>
              </div>
            ))}
          </div>
        </section>
      </main>
    </div>
  );
}

function Button({
  className,
  variant = "default",
  ...props
}: React.ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "default" | "outline";
}) {
  return (
    <button
      className={cn(
        "inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-pink-500 disabled:pointer-events-none disabled:opacity-50",
        variant === "default"
          ? "bg-pink-500 text-white hover:bg-pink-600"
          : "border border-pink-200 text-pink-500 hover:bg-pink-50",
        className
      )}
      {...props}
    />
  );
}
