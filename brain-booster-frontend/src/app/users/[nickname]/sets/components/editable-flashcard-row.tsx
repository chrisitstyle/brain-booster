"use client";

import { Pencil, Star, Volume2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
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

  return (
    <Card className="border-gray-200 bg-white transition-all hover:shadow-md print:break-inside-avoid print:shadow-none">
      <CardContent className="p-4">
        {isEditing ? (
          <div className="space-y-4">
            <div className="grid flex-1 grid-cols-1 gap-4 md:grid-cols-2">
              <div>
                <label className="text-xs font-medium uppercase tracking-wide text-gray-400">
                  Term
                </label>

                <Input
                  value={editTerm}
                  onChange={(event) => onEditTermChange(event.target.value)}
                  className="mt-1 border-gray-200 focus:border-pink-300 focus:ring-pink-200"
                />
              </div>

              <div>
                <label className="text-xs font-medium uppercase tracking-wide text-gray-400">
                  Definition
                </label>

                <Input
                  value={editDefinition}
                  onChange={(event) =>
                    onEditDefinitionChange(event.target.value)
                  }
                  className="mt-1 border-gray-200 focus:border-pink-300 focus:ring-pink-200"
                />
              </div>
            </div>

            <div className="flex justify-end gap-2 print:hidden">
              <Button
                type="button"
                variant="outline"
                size="sm"
                className="border-gray-200 text-gray-500 hover:bg-gray-50"
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
            <div className="grid flex-1 grid-cols-1 gap-4 md:grid-cols-2">
              <div>
                <button
                  type="button"
                  disabled={!shouldBlurTerm}
                  onClick={onRevealText}
                  className={cn(
                    "block max-w-full text-left",
                    shouldBlurTerm ? "cursor-pointer" : "cursor-default",
                  )}
                >
                  <span
                    className={cn(
                      "block font-medium text-gray-800 transition",
                      shouldBlurTerm && "select-none blur-sm",
                    )}
                  >
                    {flashcard.term}
                  </span>
                </button>
              </div>

              <div>
                <button
                  type="button"
                  disabled={!shouldBlurDefinition}
                  onClick={onRevealText}
                  className={cn(
                    "block max-w-full text-left",
                    shouldBlurDefinition ? "cursor-pointer" : "cursor-default",
                  )}
                >
                  <span
                    className={cn(
                      "block text-gray-600 transition",
                      shouldBlurDefinition && "select-none blur-sm",
                    )}
                  >
                    {flashcard.definition}
                  </span>
                </button>
              </div>
            </div>

            <div className="flex items-center gap-1 print:hidden">
              <Button
                type="button"
                variant="ghost"
                size="icon"
                disabled={isStarPending}
                className={cn(
                  "h-8 w-8 text-gray-400 hover:text-yellow-500 disabled:cursor-not-allowed disabled:opacity-60",
                  flashcard.starred && "text-yellow-400 hover:text-yellow-500",
                )}
                onClick={onToggleStar}
                aria-label={
                  flashcard.starred ? "Unstar flashcard" : "Star flashcard"
                }
              >
                <Star
                  className={cn(
                    "h-4 w-4",
                    flashcard.starred && "fill-yellow-400 text-yellow-400",
                  )}
                />
              </Button>

              <Button
                type="button"
                variant="ghost"
                size="icon"
                className="h-8 w-8 text-gray-400 hover:text-pink-500"
                onClick={onSpeak}
                aria-label="Read term aloud"
              >
                <Volume2 className="h-4 w-4" />
              </Button>

              <Button
                type="button"
                variant="ghost"
                size="icon"
                className="h-8 w-8 text-gray-400 hover:text-pink-500"
                onClick={onStartEditing}
                aria-label="Edit flashcard"
              >
                <Pencil className="h-4 w-4" />
              </Button>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
