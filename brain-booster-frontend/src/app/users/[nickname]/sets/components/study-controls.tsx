"use client";

import { ArrowLeft, ArrowRight, Check, Shuffle, X } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";

interface StudyControlsProps {
  currentIndex: number;
  totalCards: number;
  isProgressTrackingEnabled: boolean;
  onToggleProgressTracking: () => void;
  onUnknown: () => void;
  onPrevious: () => void;
  onNext: () => void;
  onKnown: () => void;
  onShuffle: () => void;
}

export default function StudyControls({
  currentIndex,
  totalCards,
  isProgressTrackingEnabled,
  onToggleProgressTracking,
  onUnknown,
  onPrevious,
  onNext,
  onKnown,
  onShuffle,
}: StudyControlsProps) {
  const hasCards = totalCards > 0;

  const displayedCurrentIndex = hasCards
    ? Math.min(currentIndex + 1, totalCards)
    : 0;

  const isFirstCard = !hasCards || currentIndex <= 0;

  const isLastCard = !hasCards || currentIndex >= totalCards - 1;

  return (
    <div className="mb-6 flex flex-wrap items-center justify-center gap-4 sm:gap-6">
      <div className="flex shrink-0 items-center gap-2">
        <Label
          htmlFor="track-study-progress"
          className="cursor-pointer whitespace-nowrap text-sm font-semibold text-foreground transition-colors hover:text-pink-500 dark:hover:text-pink-400"
        >
          Track progress
        </Label>

        <Switch
          id="track-study-progress"
          checked={isProgressTrackingEnabled}
          onCheckedChange={() => onToggleProgressTracking()}
          className="data-[state=checked]:bg-pink-500 data-[state=unchecked]:bg-muted-foreground/30"
          aria-label="Track study progress"
        />
      </div>

      <Button
        type="button"
        variant="outline"
        size="lg"
        className="h-14 w-14 rounded-full border-2 border-red-200 bg-background text-red-500 hover:border-red-400 hover:bg-red-50 hover:text-red-600 disabled:border-border disabled:bg-muted disabled:text-muted-foreground dark:border-red-900 dark:text-red-400 dark:hover:border-red-700 dark:hover:bg-red-950/40 dark:hover:text-red-300"
        onClick={onUnknown}
        disabled={!hasCards}
        aria-label="Mark card as unknown"
      >
        <X className="h-6 w-6" aria-hidden="true" />
      </Button>

      <div className="flex items-center gap-2">
        <Button
          type="button"
          variant="outline"
          size="icon"
          className="border-border bg-background text-muted-foreground hover:border-pink-300 hover:bg-pink-50 hover:text-pink-500 disabled:bg-muted disabled:text-muted-foreground dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-400"
          onClick={onPrevious}
          disabled={isFirstCard}
          aria-label="Previous flashcard"
        >
          <ArrowLeft className="h-5 w-5" aria-hidden="true" />
        </Button>

        <span
          className="min-w-20 text-center font-medium text-foreground"
          aria-live="polite"
          aria-label={`Card ${displayedCurrentIndex} of ${totalCards}`}
        >
          {displayedCurrentIndex} / {totalCards}
        </span>

        <Button
          type="button"
          variant="outline"
          size="icon"
          className="border-border bg-background text-muted-foreground hover:border-pink-300 hover:bg-pink-50 hover:text-pink-500 disabled:bg-muted disabled:text-muted-foreground dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-400"
          onClick={onNext}
          disabled={isLastCard}
          aria-label="Next flashcard"
        >
          <ArrowRight className="h-5 w-5" aria-hidden="true" />
        </Button>
      </div>

      <div className="flex items-center gap-3">
        <Button
          type="button"
          variant="outline"
          size="lg"
          className="h-14 w-14 rounded-full border-2 border-green-200 bg-background text-green-500 hover:border-green-400 hover:bg-green-50 hover:text-green-600 disabled:border-border disabled:bg-muted disabled:text-muted-foreground dark:border-green-900 dark:text-green-400 dark:hover:border-green-700 dark:hover:bg-green-950/40 dark:hover:text-green-300"
          onClick={onKnown}
          disabled={!hasCards}
          aria-label="Mark card as known"
        >
          <Check className="h-6 w-6" aria-hidden="true" />
        </Button>

        <Button
          type="button"
          variant="outline"
          size="icon"
          className="h-14 w-14 rounded-full border-border bg-background text-muted-foreground hover:border-pink-300 hover:bg-pink-50 hover:text-pink-500 disabled:bg-muted disabled:text-muted-foreground dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-400"
          onClick={onShuffle}
          disabled={totalCards < 2}
          aria-label="Shuffle flashcards"
        >
          <Shuffle className="h-5 w-5" aria-hidden="true" />
        </Button>
      </div>
    </div>
  );
}
