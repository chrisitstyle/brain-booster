"use client";

import type { KeyboardEvent } from "react";
import { Volume2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";

import type { StudyFlashcard } from "../[id]/types";

interface StudyFlashcardCardProps {
  currentFlashcard?: StudyFlashcard;
  isFlipped: boolean;
  onFlip: () => void;
  onSpeak: (text: string) => void;
}

export default function StudyFlashcardCard({
  currentFlashcard,
  isFlipped,
  onFlip,
  onSpeak,
}: StudyFlashcardCardProps) {
  const term = currentFlashcard?.term ?? "";
  const definition = currentFlashcard?.definition ?? "";

  function handleCardKeyDown(event: KeyboardEvent<HTMLDivElement>) {
    if (event.key !== "Enter" && event.key !== " ") {
      return;
    }

    event.preventDefault();
    onFlip();
  }

  return (
    <div
      className="perspective-1000 mb-4 cursor-pointer rounded-xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
      onClick={onFlip}
      onKeyDown={handleCardKeyDown}
      role="button"
      tabIndex={0}
      aria-label={
        isFlipped
          ? "Showing definition. Flip to term."
          : "Showing term. Flip to definition."
      }
    >
      <div
        className={cn(
          "relative h-64 w-full transition-transform duration-500 transform-style-3d md:h-80",
          isFlipped && "rotate-y-180",
        )}
        style={{
          transformStyle: "preserve-3d",
          transform: isFlipped ? "rotateY(180deg)" : "rotateY(0deg)",
        }}
      >
        <Card
          className="absolute inset-0 flex items-center justify-center overflow-hidden border-border bg-card p-8 text-card-foreground shadow-lg backface-hidden transition-colors hover:border-pink-200 dark:hover:border-pink-900"
          style={{
            backfaceVisibility: "hidden",
          }}
          aria-hidden={isFlipped}
        >
          <Button
            type="button"
            variant="ghost"
            size="icon"
            disabled={!term}
            className="absolute right-4 top-4 z-10 h-10 w-10 rounded-full text-muted-foreground hover:bg-pink-50 hover:text-pink-500 disabled:opacity-40 dark:hover:bg-pink-950/40 dark:hover:text-pink-400"
            onClick={(event) => {
              event.stopPropagation();
              onSpeak(term);
            }}
            aria-label="Read term aloud"
          >
            <Volume2 className="h-5 w-5" aria-hidden="true" />
          </Button>

          <CardContent className="flex max-h-full w-full flex-col items-center justify-center overflow-y-auto p-0 text-center">
            <p className="whitespace-pre-wrap break-words text-xl font-medium text-card-foreground md:text-2xl">
              {term || <span className="text-muted-foreground">No term</span>}
            </p>
          </CardContent>
        </Card>

        <Card
          className="absolute inset-0 flex items-center justify-center overflow-hidden border-border bg-card p-8 text-card-foreground shadow-lg transition-colors hover:border-pink-200 dark:hover:border-pink-900"
          style={{
            backfaceVisibility: "hidden",
            transform: "rotateY(180deg)",
          }}
          aria-hidden={!isFlipped}
        >
          <Button
            type="button"
            variant="ghost"
            size="icon"
            disabled={!definition}
            className="absolute right-4 top-4 z-10 h-10 w-10 rounded-full text-muted-foreground hover:bg-pink-50 hover:text-pink-500 disabled:opacity-40 dark:hover:bg-pink-950/40 dark:hover:text-pink-400"
            onClick={(event) => {
              event.stopPropagation();
              onSpeak(definition);
            }}
            aria-label="Read definition aloud"
          >
            <Volume2 className="h-5 w-5" aria-hidden="true" />
          </Button>

          <CardContent className="flex max-h-full w-full flex-col items-center justify-center overflow-y-auto p-0 text-center">
            <p className="whitespace-pre-wrap break-words text-lg text-card-foreground md:text-xl">
              {definition || (
                <span className="text-muted-foreground">No definition</span>
              )}
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
