import type { GameResult, SaveGameResultRequest } from "@/types/games";
import { apiRequest } from "@/api/apiClient";

export async function saveGameResult(
  request: SaveGameResultRequest,
  token: string,
) {
  return apiRequest<GameResult>("/game-results", {
    method: "POST",
    token,
    body: request,
  });
}

export async function getMyGameResults(token: string) {
  return apiRequest<GameResult[]>("/game-results/me", {
    token,
  });
}

export async function getMyGameResultsBySetId(setId: number, token: string) {
  return apiRequest<GameResult[]>("/game-results/me", {
    token,
    query: { setId },
  });
}

export async function getAllGameResults(token: string) {
  return apiRequest<GameResult[]>("/game-results", {
    token,
  });
}

export async function getAllGameResultsBySetId(setId: number, token: string) {
  return apiRequest<GameResult[]>("/game-results", {
    token,
    query: { setId },
  });
}

export async function getGameResultById(resultId: number, token: string) {
  return apiRequest<GameResult>(`/game-results/${resultId}`, { token });
}

export async function deleteGameResult(resultId: number, token: string) {
  return apiRequest<void>(`/game-results/${resultId}`, {
    method: "DELETE",
    token,
    responseType: "void",
  });
}

export type { GameResult, SaveGameResultRequest };
