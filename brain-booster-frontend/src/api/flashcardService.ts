import { apiRequest } from "@/api/apiClient";

export interface CreateFlashcardData {
  setId: number;
  term: string;
  definition: string;
}

export interface Flashcard {
  flashcardId: number;
  setId: number;
  term: string;
  definition: string;
  starred: boolean;
}

export interface UpdateFlashcardData {
  term: string;
  definition: string;
}

export async function addFlashcard(
  data: CreateFlashcardData,
  token: string,
): Promise<Flashcard> {
  return apiRequest<Flashcard>("/flashcards", {
    method: "POST",
    token,
    body: data,
    fallbackMessage: "Failed to create flashcard",
  });
}

export async function getFlashcardsBySetId(
  setId: string | number,
  token?: string | null,
): Promise<Flashcard[]> {
  return apiRequest<Flashcard[]>(`/flashcard-sets/${setId}/flashcards`, {
    token,
    fallbackMessage: "Failed to fetch flashcards",
  });
}

export async function updateFlashcardById(
  flashcardId: string | number,
  data: UpdateFlashcardData,
  token: string,
): Promise<Flashcard> {
  return apiRequest<Flashcard>(`/flashcards/${flashcardId}`, {
    method: "PATCH",
    token,
    body: data,
    fallbackMessage: "Failed to update flashcard",
  });
}

export async function deleteFlashcard(
  flashcardId: string | number,
  token: string,
): Promise<void> {
  return apiRequest<void>(`/flashcards/${flashcardId}`, {
    method: "DELETE",
    token,
    responseType: "void",
    fallbackMessage: "Failed to delete flashcard",
  });
}

export async function starFlashcard(
  flashcardId: string | number,
  token: string,
): Promise<Flashcard> {
  return apiRequest<Flashcard>(`/flashcards/${flashcardId}/starred`, {
    method: "POST",
    token,
    fallbackMessage: "Failed to star flashcard",
  });
}

export async function unstarFlashcard(
  flashcardId: string | number,
  token: string,
): Promise<Flashcard> {
  return apiRequest<Flashcard>(`/flashcards/${flashcardId}/starred`, {
    method: "DELETE",
    token,
    fallbackMessage: "Failed to unstar flashcard",
  });
}
