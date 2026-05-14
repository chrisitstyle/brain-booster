"use client";

import type { StudyFlashcard } from "../[id]/types";
import EditableFlashcardRow from "./editable-flashcard-row";

interface FlashcardTermsListProps {
  flashcards: StudyFlashcard[];
  editingFlashcardId: number | null;
  editTerm: string;
  editDefinition: string;
  onEditTermChange: (value: string) => void;
  onEditDefinitionChange: (value: string) => void;
  onStartEditing: (flashcard: StudyFlashcard) => void;
  onCancelEditing: () => void;
  onSaveEditing: (flashcardId: number) => Promise<void>;
  onToggleStar: (flashcardId: number) => void;
  onSpeak: (text: string) => void;
}

export default function FlashcardTermsList({
  flashcards,
  editingFlashcardId,
  editTerm,
  editDefinition,
  onEditTermChange,
  onEditDefinitionChange,
  onStartEditing,
  onCancelEditing,
  onSaveEditing,
  onToggleStar,
  onSpeak,
}: FlashcardTermsListProps) {
  return (
    <div className="mx-auto max-w-2xl print:mx-0 print:max-w-none">
      <h2 className="mb-4 text-xl font-semibold text-gray-800">
        Terms in this set ({flashcards.length})
      </h2>

      <div className="space-y-3 print:space-y-2">
        {flashcards.map((flashcard) => (
          <EditableFlashcardRow
            key={flashcard.flashcardId}
            flashcard={flashcard}
            isEditing={editingFlashcardId === flashcard.flashcardId}
            editTerm={editTerm}
            editDefinition={editDefinition}
            onEditTermChange={onEditTermChange}
            onEditDefinitionChange={onEditDefinitionChange}
            onStartEditing={() => onStartEditing(flashcard)}
            onCancelEditing={onCancelEditing}
            onSaveEditing={() => {
              void onSaveEditing(flashcard.flashcardId);
            }}
            onToggleStar={() => onToggleStar(flashcard.flashcardId)}
            onSpeak={() => onSpeak(flashcard.term)}
          />
        ))}
      </div>
    </div>
  );
}
