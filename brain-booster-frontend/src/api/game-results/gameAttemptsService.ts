import type {
  GameAttempt,
  GameAttemptFilters,
  GameQuestionResult,
  PageResponse,
} from "@/types/games";

import {
  buildQueryString,
  createAuthHeaders,
  fetchJson,
  getApiUrl,
} from "./apiClient";

export async function getMyGameAttempts(
  token: string,
  filters: GameAttemptFilters = {},
) {
  const queryString = buildQueryString({
    page: filters.page,
    size: filters.size,
    setId: filters.setId,
    mode: filters.mode,
    from: filters.from,
    to: filters.to,
  });

  return fetchJson<PageResponse<GameAttempt>>(
    getApiUrl(`/game-attempts/me${queryString}`),
    {
      method: "GET",
      headers: createAuthHeaders(token),
    },
  );
}

export async function getMyGameAttemptsBySetId(
  setId: number,
  token: string,
  filters: Omit<GameAttemptFilters, "setId"> = {},
) {
  const queryString = buildQueryString({
    page: filters.page,
    size: filters.size,
    mode: filters.mode,
    from: filters.from,
    to: filters.to,
  });

  return fetchJson<PageResponse<GameAttempt>>(
    getApiUrl(`/game-attempts/me/sets/${setId}${queryString}`),
    {
      method: "GET",
      headers: createAuthHeaders(token),
    },
  );
}

export async function getGameAttemptById(attemptId: number, token: string) {
  return fetchJson<GameAttempt>(getApiUrl(`/game-attempts/${attemptId}`), {
    method: "GET",
    headers: createAuthHeaders(token),
  });
}

export async function getGameAttemptQuestionResults(
  attemptId: number,
  token: string,
) {
  return fetchJson<GameQuestionResult[]>(
    getApiUrl(`/game-attempts/${attemptId}/question-results`),
    {
      method: "GET",
      headers: createAuthHeaders(token),
    },
  );
}

export type {
  GameAttempt,
  GameAttemptFilters,
  GameQuestionResult,
  PageResponse,
};
