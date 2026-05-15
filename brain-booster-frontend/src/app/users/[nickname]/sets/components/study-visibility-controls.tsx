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
      <div className="flex max-w-full items-center gap-2 rounded-full border border-gray-200 bg-white px-2 py-2 shadow-lg">
        <button
          type="button"
          aria-pressed={areTermsHidden}
          onClick={onToggleTerms}
          className={cn(
            "inline-flex h-10 items-center gap-2 rounded-full px-4 text-sm font-semibold transition-all",
            areTermsHidden
              ? "bg-pink-500 text-white hover:bg-pink-600"
              : "text-gray-600 hover:bg-pink-50 hover:text-pink-500",
          )}
        >
          {areTermsHidden ? (
            <Eye className="h-4 w-4" />
          ) : (
            <EyeOff className="h-4 w-4" />
          )}

          {areTermsHidden ? "Show terms" : "Hide terms"}
        </button>

        <div className="h-6 w-px bg-gray-200" />

        <button
          type="button"
          aria-pressed={areDefinitionsHidden}
          onClick={onToggleDefinitions}
          className={cn(
            "inline-flex h-10 items-center gap-2 rounded-full px-4 text-sm font-semibold transition-all",
            areDefinitionsHidden
              ? "bg-pink-500 text-white hover:bg-pink-600"
              : "text-gray-600 hover:bg-pink-50 hover:text-pink-500",
          )}
        >
          {areDefinitionsHidden ? (
            <Eye className="h-4 w-4" />
          ) : (
            <EyeOff className="h-4 w-4" />
          )}

          {areDefinitionsHidden ? "Show definitions" : "Hide definitions"}
        </button>
      </div>
    </div>
  );
}
