"use client";

import { ArrowLeft, ArrowRight, Check, Shuffle, X } from "lucide-react";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";

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
  return (
    <div className="mb-6 flex flex-wrap items-center justify-center gap-4 sm:gap-6">
      <button
        type="button"
        role="switch"
        aria-checked={isProgressTrackingEnabled}
        onClick={onToggleProgressTracking}
        className="flex shrink-0 appearance-none items-center gap-2 border-0 bg-transparent p-0 text-sm font-semibold text-gray-700 shadow-none outline-none transition-colors hover:text-pink-500 focus-visible:outline-none focus-visible:ring-0"
      >
        <span className="whitespace-nowrap">Track progress</span>

        <span
          className={cn(
            "relative inline-flex h-5 w-9 shrink-0 rounded-full p-0.5 transition-colors",
            isProgressTrackingEnabled ? "bg-pink-500" : "bg-gray-300",
          )}
        >
          <span
            className={cn(
              "h-4 w-4 rounded-full bg-white shadow-sm transition-transform",
              isProgressTrackingEnabled ? "translate-x-4" : "translate-x-0",
            )}
          />
        </span>
      </button>

      <Button
        variant="outline"
        size="lg"
        className="h-14 w-14 rounded-full border-2 border-red-200 text-red-500 hover:border-red-400 hover:bg-red-50"
        onClick={onUnknown}
      >
        <X className="h-6 w-6" />
      </Button>

      <div className="flex items-center gap-2">
        <Button
          variant="outline"
          size="icon"
          className="border-gray-200 text-gray-500 hover:border-pink-300 hover:text-pink-500"
          onClick={onPrevious}
          disabled={currentIndex === 0}
        >
          <ArrowLeft className="h-5 w-5" />
        </Button>

        <span className="min-w-20 text-center text-gray-600">
          {currentIndex + 1} / {totalCards}
        </span>

        <Button
          variant="outline"
          size="icon"
          className="border-gray-200 text-gray-500 hover:border-pink-300 hover:text-pink-500"
          onClick={onNext}
          disabled={currentIndex === totalCards - 1}
        >
          <ArrowRight className="h-5 w-5" />
        </Button>
      </div>

      <div className="flex items-center gap-3">
        <Button
          variant="outline"
          size="lg"
          className="h-14 w-14 rounded-full border-2 border-green-200 text-green-500 hover:border-green-400 hover:bg-green-50"
          onClick={onKnown}
        >
          <Check className="h-6 w-6" />
        </Button>

        <Button
          variant="outline"
          size="icon"
          className="h-14 w-14 rounded-full border-gray-200 text-gray-500 hover:border-pink-300 hover:text-pink-500"
          onClick={onShuffle}
        >
          <Shuffle className="h-5 w-5" />
        </Button>
      </div>
    </div>
  );
}
