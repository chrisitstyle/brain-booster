"use client";

import { Pencil, Star, Volume2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { cn } from "@/lib/utils";

import type { StudyFlashcard } from "../[id]/types";

interface EditableFlashcardRowProps {
  flashcard: StudyFlashcard;
  isEditing: boolean;
  editTerm: string;
  editDefinition: string;
  areTermsHidden: boolean;
  areDefinitionsHidden: boolean;
  isTextRevealed: boolean;
  isStarPending: boolean;
  onRevealText: () => void;
  onEditTermChange: (value: string) => void;
  onEditDefinitionChange: (value: string) => void;
  onStartEditing: () => void;
  onCancelEditing: () => void;
  onSaveEditing: () => void;
  onToggleStar: () => void;
  onSpeak: () => void;
}

export default function EditableFlashcardRow({
  flashcard,
  isEditing,
  editTerm,
  editDefinition,
  areTermsHidden,
  areDefinitionsHidden,
  isTextRevealed,
  isStarPending,
  onRevealText,
  onEditTermChange,
  onEditDefinitionChange,
  onStartEditing,
  onCancelEditing,
  onSaveEditing,
  onToggleStar,
  onSpeak,
}: EditableFlashcardRowProps) {
  const shouldBlurTerm = areTermsHidden && !isTextRevealed;

  const shouldBlurDefinition = areDefinitionsHidden && !isTextRevealed;

  const termInputId = `flashcard-${flashcard.flashcardId}-term`;
  const definitionInputId = `flashcard-${flashcard.flashcardId}-definition`;

  return (
    <Card className="border-border bg-card text-card-foreground transition-all hover:border-pink-200 hover:shadow-md dark:hover:border-pink-900 print:break-inside-avoid print:border-gray-300 print:bg-white print:text-black print:shadow-none">
      <CardContent className="p-4">
        {isEditing ? (
          <div className="space-y-4">
            <div className="grid flex-1 grid-cols-1 gap-4 md:grid-cols-2">
              <div>
                <Label
                  htmlFor={termInputId}
                  className="text-xs font-medium uppercase tracking-wide text-muted-foreground"
                >
                  Term
                </Label>

                <Input
                  id={termInputId}
                  value={editTerm}
                  onChange={(event) => onEditTermChange(event.target.value)}
                  className="mt-1 border-input bg-background text-foreground placeholder:text-muted-foreground focus-visible:border-pink-300 focus-visible:ring-pink-500/20 dark:focus-visible:border-pink-800"
                  autoComplete="off"
                />
              </div>

              <div>
                <Label
                  htmlFor={definitionInputId}
                  className="text-xs font-medium uppercase tracking-wide text-muted-foreground"
                >
                  Definition
                </Label>

                <Input
                  id={definitionInputId}
                  value={editDefinition}
                  onChange={(event) =>
                    onEditDefinitionChange(event.target.value)
                  }
                  className="mt-1 border-input bg-background text-foreground placeholder:text-muted-foreground focus-visible:border-pink-300 focus-visible:ring-pink-500/20 dark:focus-visible:border-pink-800"
                  autoComplete="off"
                />
              </div>
            </div>

            <div className="flex justify-end gap-2 print:hidden">
              <Button
                type="button"
                variant="outline"
                size="sm"
                className="border-border bg-background text-muted-foreground hover:bg-accent hover:text-foreground"
                onClick={onCancelEditing}
              >
                Cancel
              </Button>

              <Button
                type="button"
                size="sm"
                className="bg-pink-500 text-white hover:bg-pink-600"
                onClick={onSaveEditing}
              >
                Save
              </Button>
            </div>
          </div>
        ) : (
          <div className="flex items-start justify-between gap-4">
            <div className="grid min-w-0 flex-1 grid-cols-1 gap-4 md:grid-cols-2">
              <div className="min-w-0">
                {shouldBlurTerm ? (
                  <button
                    type="button"
                    onClick={onRevealText}
                    className="block max-w-full cursor-pointer rounded-sm text-left focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                    aria-label="Reveal flashcard term"
                  >
                    <span
                      className="block select-none break-words font-medium text-card-foreground blur-sm transition"
                      aria-hidden="true"
                    >
                      {flashcard.term}
                    </span>
                  </button>
                ) : (
                  <p className="whitespace-pre-wrap break-words font-medium text-card-foreground print:text-black">
                    {flashcard.term}
                  </p>
                )}
              </div>

              <div className="min-w-0">
                {shouldBlurDefinition ? (
                  <button
                    type="button"
                    onClick={onRevealText}
                    className="block max-w-full cursor-pointer rounded-sm text-left focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                    aria-label="Reveal flashcard definition"
                  >
                    <span
                      className="block select-none break-words text-muted-foreground blur-sm transition"
                      aria-hidden="true"
                    >
                      {flashcard.definition}
                    </span>
                  </button>
                ) : (
                  <p className="whitespace-pre-wrap break-words text-muted-foreground print:text-black">
                    {flashcard.definition}
                  </p>
                )}
              </div>
            </div>

            <div className="flex shrink-0 items-center gap-1 print:hidden">
              <Button
                type="button"
                variant="ghost"
                size="icon"
                disabled={isStarPending}
                className={cn(
                  "h-8 w-8 text-muted-foreground hover:bg-yellow-50 hover:text-yellow-500 disabled:cursor-not-allowed disabled:opacity-60 dark:hover:bg-yellow-950/40",
                  flashcard.starred &&
                    "text-yellow-500 hover:text-yellow-600 dark:text-yellow-400 dark:hover:text-yellow-300",
                )}
                onClick={onToggleStar}
                aria-label={
                  flashcard.starred ? "Unstar flashcard" : "Star flashcard"
                }
                aria-pressed={flashcard.starred}
                aria-busy={isStarPending}
              >
                <Star
                  className={cn("h-4 w-4", flashcard.starred && "fill-current")}
                  aria-hidden="true"
                />
              </Button>

              <Button
                type="button"
                variant="ghost"
                size="icon"
                disabled={!flashcard.term.trim()}
                className="h-8 w-8 text-muted-foreground hover:bg-pink-50 hover:text-pink-500 dark:hover:bg-pink-950/40 dark:hover:text-pink-400"
                onClick={onSpeak}
                aria-label="Read term aloud"
              >
                <Volume2 className="h-4 w-4" aria-hidden="true" />
              </Button>

              <Button
                type="button"
                variant="ghost"
                size="icon"
                className="h-8 w-8 text-muted-foreground hover:bg-pink-50 hover:text-pink-500 dark:hover:bg-pink-950/40 dark:hover:text-pink-400"
                onClick={onStartEditing}
                aria-label="Edit flashcard"
              >
                <Pencil className="h-4 w-4" aria-hidden="true" />
              </Button>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
