import type {
  GameAnalyticsSummary,
  GameProgressPoint,
  QuestionTypeAnalytics,
  WeakFlashcard,
} from "@/types/games";

import { createAuthHeaders, fetchJson, getApiUrl } from "./apiClient";

export async function getMySetGameAnalyticsSummary(
  setId: number,
  token: string,
) {
  return fetchJson<GameAnalyticsSummary>(
    getApiUrl(`/game-analytics/me/sets/${setId}/summary`),
    {
      method: "GET",
      headers: createAuthHeaders(token),
    },
  );
}

export async function getMySetGameProgress(setId: number, token: string) {
  return fetchJson<GameProgressPoint[]>(
    getApiUrl(`/game-analytics/me/sets/${setId}/progress`),
    {
      method: "GET",
      headers: createAuthHeaders(token),
    },
  );
}

export async function getMySetWeakFlashcards(setId: number, token: string) {
  return fetchJson<WeakFlashcard[]>(
    getApiUrl(`/game-analytics/me/sets/${setId}/weak-flashcards`),
    {
      method: "GET",
      headers: createAuthHeaders(token),
    },
  );
}

export async function getMySetQuestionTypeAnalytics(
  setId: number,
  token: string,
) {
  return fetchJson<QuestionTypeAnalytics[]>(
    getApiUrl(`/game-analytics/me/sets/${setId}/question-types`),
    {
      method: "GET",
      headers: createAuthHeaders(token),
    },
  );
}

export type {
  GameAnalyticsSummary,
  GameProgressPoint,
  QuestionTypeAnalytics,
  WeakFlashcard,
};
