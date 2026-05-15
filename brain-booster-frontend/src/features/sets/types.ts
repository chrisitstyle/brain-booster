export interface FlashcardEditorItem {
  localId: string;
  flashcardId?: number;
  term: string;
  definition: string;
}

export type FlashcardEditorField = "term" | "definition";

export interface ImportedFlashcard {
  term: string;
  definition: string;
}
