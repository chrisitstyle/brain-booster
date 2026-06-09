import { getApiBaseUrl } from "./apiConfig";

export type GameMode =
  | "multiple-choice"
  | "written"
  | "matching"
  | "custom-test";

export interface GameResult {
  resultId: number;
  userId: number;
  setId: number;
  mode: GameMode;
  score: number;
  totalQuestions: number;
  durationSeconds?: number | null;
  completedAt: string;
}

export interface SaveGameResultRequest {
  setId: number;
  mode: GameMode;
  score: number;
  totalQuestions: number;
  durationSeconds?: number;
}

export async function saveGameResult(
  request: SaveGameResultRequest,
  token: string,
): Promise<GameResult> {
  const response = await fetch(`${getApiBaseUrl()}/game-results`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    throw new Error("Failed to save game result.");
  }

  return response.json();
}

export async function getMyGameResults(
  token: string,
  setId?: number | string,
): Promise<GameResult[]> {
  const searchParams = new URLSearchParams();

  if (setId !== undefined) {
    searchParams.set("setId", String(setId));
  }

  const queryString = searchParams.toString();

  const response = await fetch(
    `${getApiBaseUrl()}/game-results/me${queryString ? `?${queryString}` : ""}`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    },
  );

  if (!response.ok) {
    throw new Error("Failed to fetch game results.");
  }

  return response.json();
}

export async function getGameResultById(
  resultId: number,
  token: string,
): Promise<GameResult> {
  const response = await fetch(`${getApiBaseUrl()}/game-results/${resultId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Failed to fetch game result.");
  }

  return response.json();
}

export async function deleteGameResult(
  resultId: number,
  token: string,
): Promise<void> {
  const response = await fetch(`${getApiBaseUrl()}/game-results/${resultId}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Failed to delete game result.");
  }
}
