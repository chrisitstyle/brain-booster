import type { StudyFlashcard } from "../[id]/types";
import EditableFlashcardRow from "./editable-flashcard-row";

interface FlashcardTermsListProps {
  flashcards: StudyFlashcard[];
  editingFlashcardId: number | null;
  editTerm: string;
  editDefinition: string;
  areTermsHidden: boolean;
  areDefinitionsHidden: boolean;
  revealedFlashcardIds: Set<number>;
  pendingStarFlashcardIds: Set<number>;
  onRevealFlashcardText: (flashcardId: number) => void;
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
  areTermsHidden,
  areDefinitionsHidden,
  revealedFlashcardIds,
  pendingStarFlashcardIds,
  onRevealFlashcardText,
  onEditTermChange,
  onEditDefinitionChange,
  onStartEditing,
  onCancelEditing,
  onSaveEditing,
  onToggleStar,
  onSpeak,
}: FlashcardTermsListProps) {
  return (
    <section
      className="mx-auto max-w-2xl print:mx-0 print:max-w-none"
      aria-labelledby="flashcard-terms-heading"
    >
      <h2
        id="flashcard-terms-heading"
        className="mb-4 text-xl font-semibold text-foreground print:text-black"
      >
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
            areTermsHidden={areTermsHidden}
            areDefinitionsHidden={areDefinitionsHidden}
            isTextRevealed={revealedFlashcardIds.has(flashcard.flashcardId)}
            isStarPending={pendingStarFlashcardIds.has(flashcard.flashcardId)}
            onRevealText={() => onRevealFlashcardText(flashcard.flashcardId)}
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
    </section>
  );
}
