import type { GameResult, SaveGameResultRequest } from "@/types/games";

import {
  createAuthHeaders,
  fetchJson,
  fetchWithoutBody,
  getApiUrl,
} from "./apiClient";

export async function saveGameResult(
  request: SaveGameResultRequest,
  token: string,
) {
  return fetchJson<GameResult>(getApiUrl("/game-results"), {
    method: "POST",
    headers: createAuthHeaders(token),
    body: JSON.stringify(request),
  });
}

export async function getMyGameResults(token: string) {
  return fetchJson<GameResult[]>(getApiUrl("/game-results/me"), {
    method: "GET",
    headers: createAuthHeaders(token),
  });
}

export async function getMyGameResultsBySetId(setId: number, token: string) {
  return fetchJson<GameResult[]>(getApiUrl(`/game-results/me?setId=${setId}`), {
    method: "GET",
    headers: createAuthHeaders(token),
  });
}

export async function getAllGameResults(token: string) {
  return fetchJson<GameResult[]>(getApiUrl("/game-results"), {
    method: "GET",
    headers: createAuthHeaders(token),
  });
}

export async function getAllGameResultsBySetId(setId: number, token: string) {
  return fetchJson<GameResult[]>(getApiUrl(`/game-results?setId=${setId}`), {
    method: "GET",
    headers: createAuthHeaders(token),
  });
}

export async function getGameResultById(resultId: number, token: string) {
  return fetchJson<GameResult>(getApiUrl(`/game-results/${resultId}`), {
    method: "GET",
    headers: createAuthHeaders(token),
  });
}

export async function deleteGameResult(resultId: number, token: string) {
  return fetchWithoutBody(getApiUrl(`/game-results/${resultId}`), {
    method: "DELETE",
    headers: createAuthHeaders(token),
  });
}

export type { GameResult, SaveGameResultRequest };
