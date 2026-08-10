import type {
  GameAnalyticsSummary,
  GameProgressPoint,
  QuestionTypeAnalytics,
  WeakFlashcard,
} from "@/types/games";

import { apiRequest } from "@/api/apiClient";

export async function getMySetGameAnalyticsSummary(
  setId: number,
  token: string,
) {
  return apiRequest<GameAnalyticsSummary>(
    `/game-analytics/me/sets/${setId}/summary`,
    { token },
  );
}

export async function getMySetGameProgress(setId: number, token: string) {
  return apiRequest<GameProgressPoint[]>(
    `/game-analytics/me/sets/${setId}/progress`,
    { token },
  );
}

export async function getMySetWeakFlashcards(setId: number, token: string) {
  return apiRequest<WeakFlashcard[]>(
    `/game-analytics/me/sets/${setId}/weak-flashcards`,
    { token },
  );
}

export async function getMySetQuestionTypeAnalytics(
  setId: number,
  token: string,
) {
  return apiRequest<QuestionTypeAnalytics[]>(
    `/game-analytics/me/sets/${setId}/question-types`,
    { token },
  );
}

export type {
  GameAnalyticsSummary,
  GameProgressPoint,
  QuestionTypeAnalytics,
  WeakFlashcard,
};
