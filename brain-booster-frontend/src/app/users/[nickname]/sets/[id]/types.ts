import type { Flashcard } from "@/api/flashcardService";

export type StudyFlashcard = Flashcard & {
  starred: boolean;
};
