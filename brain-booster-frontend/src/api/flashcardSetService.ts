import { apiRequest } from "@/api/apiClient";

export interface CreateFlashcardSetData {
  setName: string;
  description: string;
}

export interface UpdateFlashcardSetData {
  setName: string;
  description: string;
}

export interface FlashcardSet {
  setId: number;
  user: {
    nickname: string;
    createdAt: string;
  };
  setName: string;
  description: string;
  createdAt: string;
  termCount: number;
}

export async function addFlashcardSet(
  data: CreateFlashcardSetData,
  token: string,
): Promise<FlashcardSet> {
  return apiRequest<FlashcardSet>("/flashcard-sets", {
    method: "POST",
    token,
    body: data,
    fallbackMessage: "Failed to create study set",
  });
}

export async function getFlashcardSetById(
  setId: string | number,
): Promise<FlashcardSet> {
  return apiRequest<FlashcardSet>(`/flashcard-sets/${setId}`, {
    fallbackMessage: "Failed to fetch flashcard set details",
  });
}

export async function updateFlashcardSetById(
  setId: string | number,
  data: UpdateFlashcardSetData,
  token: string,
): Promise<FlashcardSet> {
  return apiRequest<FlashcardSet>(`/flashcard-sets/${setId}`, {
    method: "PATCH",
    token,
    body: data,
    fallbackMessage: "Failed to update flashcard set",
  });
}

export async function deleteFlashcardSet(
  setId: string | number,
  token: string,
): Promise<void> {
  return apiRequest<void>(`/flashcard-sets/${setId}`, {
    method: "DELETE",
    token,
    responseType: "void",
    fallbackMessage: "Failed to delete flashcard set",
  });
}
