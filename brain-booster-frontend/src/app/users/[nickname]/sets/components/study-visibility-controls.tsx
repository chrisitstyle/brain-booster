"use client";

import { Eye, EyeOff } from "lucide-react";

import { cn } from "@/lib/utils";

interface StudyVisibilityControlsProps {
  areTermsHidden: boolean;
  areDefinitionsHidden: boolean;
  onToggleTerms: () => void;
  onToggleDefinitions: () => void;
}

export default function StudyVisibilityControls({
  areTermsHidden,
  areDefinitionsHidden,
  onToggleTerms,
  onToggleDefinitions,
}: StudyVisibilityControlsProps) {
  return (
    <div className="fixed inset-x-0 bottom-6 z-50 flex justify-center px-4 print:hidden">
      <div
        className="flex max-w-full items-center gap-2 overflow-x-auto rounded-full border border-border bg-card/95 px-2 py-2 text-card-foreground shadow-lg backdrop-blur supports-[backdrop-filter]:bg-card/80"
        role="group"
        aria-label="Flashcard visibility settings"
      >
        <button
          type="button"
          aria-pressed={areTermsHidden}
          aria-label={
            areTermsHidden ? "Show flashcard terms" : "Hide flashcard terms"
          }
          onClick={onToggleTerms}
          className={cn(
            "inline-flex h-10 shrink-0 items-center gap-2 rounded-full px-4 text-sm font-semibold transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background",
            areTermsHidden
              ? "bg-pink-500 text-white hover:bg-pink-600"
              : "text-muted-foreground hover:bg-pink-50 hover:text-pink-500 dark:hover:bg-pink-950/40 dark:hover:text-pink-400",
          )}
        >
          {areTermsHidden ? (
            <Eye className="h-4 w-4" aria-hidden="true" />
          ) : (
            <EyeOff className="h-4 w-4" aria-hidden="true" />
          )}

          {areTermsHidden ? "Show terms" : "Hide terms"}
        </button>

        <div className="h-6 w-px shrink-0 bg-border" aria-hidden="true" />

        <button
          type="button"
          aria-pressed={areDefinitionsHidden}
          aria-label={
            areDefinitionsHidden
              ? "Show flashcard definitions"
              : "Hide flashcard definitions"
          }
          onClick={onToggleDefinitions}
          className={cn(
            "inline-flex h-10 shrink-0 items-center gap-2 rounded-full px-4 text-sm font-semibold transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background",
            areDefinitionsHidden
              ? "bg-pink-500 text-white hover:bg-pink-600"
              : "text-muted-foreground hover:bg-pink-50 hover:text-pink-500 dark:hover:bg-pink-950/40 dark:hover:text-pink-400",
          )}
        >
          {areDefinitionsHidden ? (
            <Eye className="h-4 w-4" aria-hidden="true" />
          ) : (
            <EyeOff className="h-4 w-4" aria-hidden="true" />
          )}

          {areDefinitionsHidden ? "Show definitions" : "Hide definitions"}
        </button>
      </div>
    </div>
  );
}
