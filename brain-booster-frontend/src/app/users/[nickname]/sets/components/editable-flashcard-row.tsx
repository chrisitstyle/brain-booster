"use client";

import { Pencil, Star, Volume2 } from "lucide-react";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";

import type { StudyFlashcard } from "../[id]/types";

interface EditableFlashcardRowProps {
  flashcard: StudyFlashcard;
  isEditing: boolean;
  editTerm: string;
  editDefinition: string;
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
  onEditTermChange,
  onEditDefinitionChange,
  onStartEditing,
  onCancelEditing,
  onSaveEditing,
  onToggleStar,
  onSpeak,
}: EditableFlashcardRowProps) {
  return (
    <Card className="border-gray-200 bg-white transition-all hover:shadow-md print:break-inside-avoid print:shadow-none">
      <CardContent className="p-4">
        {isEditing ? (
          <div className="space-y-4">
            <div className="flex-1 grid grid-cols-1 gap-4 md:grid-cols-2">
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
                variant="outline"
                size="sm"
                className="border-gray-200 text-gray-500 hover:bg-gray-50"
                onClick={onCancelEditing}
              >
                Cancel
              </Button>

              <Button
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
            <div className="flex-1 grid grid-cols-1 gap-4 md:grid-cols-2">
              <div>
                <p className="font-medium text-gray-800">{flashcard.term}</p>
              </div>

              <div>
                <p className="text-gray-600">{flashcard.definition}</p>
              </div>
            </div>

            <div className="flex items-center gap-1 print:hidden">
              <Button
                variant="ghost"
                size="icon"
                className="h-8 w-8 text-gray-400 hover:text-pink-500"
                onClick={onToggleStar}
              >
                <Star
                  className={cn(
                    "h-4 w-4",
                    flashcard.starred && "fill-yellow-400 text-yellow-400",
                  )}
                />
              </Button>

              <Button
                variant="ghost"
                size="icon"
                className="h-8 w-8 text-gray-400 hover:text-pink-500"
                onClick={onSpeak}
              >
                <Volume2 className="h-4 w-4" />
              </Button>

              <Button
                variant="ghost"
                size="icon"
                className="h-8 w-8 text-gray-400 hover:text-pink-500"
                onClick={onStartEditing}
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
