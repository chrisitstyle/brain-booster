import { useState } from "react";
import type { FlashcardEditorField, FlashcardEditorItem } from "../types";

interface UseFlashcardEditorProps {
  initialFlashcards: FlashcardEditorItem[];
}

function createEmptyFlashcard(
  localId = crypto.randomUUID(),
): FlashcardEditorItem {
  return {
    localId,
    term: "",
    definition: "",
  };
}

export function useFlashcardEditor({
  initialFlashcards,
}: UseFlashcardEditorProps) {
  const [flashcards, setFlashcards] = useState<FlashcardEditorItem[]>(() =>
    initialFlashcards.length > 0
      ? initialFlashcards
      : [createEmptyFlashcard("initial-card-1")],
  );

  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");

  const isFiltering = searchQuery.trim().length > 0;

  const filteredFlashcards = flashcards.filter((flashcard) => {
    const normalizedQuery = searchQuery.trim().toLowerCase();

    if (!normalizedQuery) {
      return true;
    }

    return (
      flashcard.term.toLowerCase().includes(normalizedQuery) ||
      flashcard.definition.toLowerCase().includes(normalizedQuery)
    );
  });

  const validFlashcards = flashcards.filter(
    (flashcard) => flashcard.term.trim() && flashcard.definition.trim(),
  );

  const addEmptyFlashcard = () => {
    setFlashcards((previousFlashcards) => [
      ...previousFlashcards,
      createEmptyFlashcard(),
    ]);
  };

  const removeFlashcard = (localId: string) => {
    if (flashcards.length <= 1) {
      return;
    }

    setFlashcards((previousFlashcards) =>
      previousFlashcards.filter((flashcard) => flashcard.localId !== localId),
    );
  };

  const updateFlashcard = (
    localId: string,
    field: FlashcardEditorField,
    value: string,
  ) => {
    setFlashcards((previousFlashcards) =>
      previousFlashcards.map((flashcard) =>
        flashcard.localId === localId
          ? { ...flashcard, [field]: value }
          : flashcard,
      ),
    );
  };

  const reorderFlashcards = (activeId: string, overId: string) => {
    if (activeId === overId) {
      return;
    }

    setFlashcards((previousFlashcards) => {
      const oldIndex = previousFlashcards.findIndex(
        (flashcard) => flashcard.localId === activeId,
      );

      const newIndex = previousFlashcards.findIndex(
        (flashcard) => flashcard.localId === overId,
      );

      if (oldIndex === -1 || newIndex === -1) {
        return previousFlashcards;
      }

      const nextFlashcards = [...previousFlashcards];
      const [movedFlashcard] = nextFlashcards.splice(oldIndex, 1);
      nextFlashcards.splice(newIndex, 0, movedFlashcard);

      return nextFlashcards;
    });
  };

  const flipTermsAndDefinitions = () => {
    setFlashcards((previousFlashcards) =>
      previousFlashcards.map((flashcard) => ({
        ...flashcard,
        term: flashcard.definition,
        definition: flashcard.term,
      })),
    );
  };

  const resetFlashcards = (nextFlashcards?: FlashcardEditorItem[]) => {
    setFlashcards(
      nextFlashcards && nextFlashcards.length > 0
        ? nextFlashcards
        : [createEmptyFlashcard()],
    );
  };

  const toggleSearch = () => {
    setIsSearchOpen((previousValue) => !previousValue);
    setSearchQuery("");
  };

  const closeSearch = () => {
    setIsSearchOpen(false);
    setSearchQuery("");
  };

  return {
    flashcards,
    filteredFlashcards,
    validFlashcards,
    isFiltering,
    isSearchOpen,
    searchQuery,
    setSearchQuery,
    toggleSearch,
    closeSearch,
    addEmptyFlashcard,
    removeFlashcard,
    updateFlashcard,
    reorderFlashcards,
    flipTermsAndDefinitions,
    resetFlashcards,
  };
}
