import { apiRequest } from "@/api/apiClient";
import type { FlashcardSet } from "@/api/flashcardSetService";

export const getUserFlashcardSetsByUserId = async (
  userId: number,
  token: string,
): Promise<FlashcardSet[]> => {
  return apiRequest<FlashcardSet[]>(`/users/${userId}/flashcard-sets`, {
    token,
    fallbackMessage: "Cannot fetch flashcard sets for user with this ID",
  });
};

export const getUserFlashcardSetsByNickname = async (
  nickname: string,
): Promise<FlashcardSet[]> => {
  return apiRequest<FlashcardSet[]>(
    `/users/nickname/${nickname}/flashcard-sets`,
    {
      fallbackMessage: "Failed to fetch flashcard sets",
    },
  );
};
