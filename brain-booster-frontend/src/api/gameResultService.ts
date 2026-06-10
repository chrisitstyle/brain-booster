import { getApiBaseUrl } from "./apiConfig";
import type {
  GameMode,
  GameQuestionType,
  QuestionAnswerSide,
} from "@/types/gameResultTypes";

export type {
  GameMode,
  GameQuestionType,
  QuestionAnswerSide,
} from "@/types/gameResultTypes";

export interface GameQuestionResult {
  questionResultId: number;
  attemptId: number;
  flashcardId: number;
  questionKey?: string | null;
  questionOrder: number;
  questionType: GameQuestionType;
  answerWith?: QuestionAnswerSide | null;
  prompt?: string | null;
  userAnswer?: string | null;
  correctAnswer?: string | null;
  wasCorrect: boolean;
  mistakesCount: number;
  answeredAt: string;
}

export interface GameAttempt {
  attemptId: number;
  userId: number;
  setId: number;
  mode: GameMode;
  score: number;
  totalQuestions: number;
  durationSeconds?: number | null;
  completedAt: string;
  questionResults: GameQuestionResult[];
}

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

export interface SaveGameQuestionResultRequest {
  flashcardId: number;
  questionKey?: string;
  questionOrder: number;
  questionType: GameQuestionType;
  answerWith?: QuestionAnswerSide;
  prompt?: string;
  userAnswer?: string;
  correctAnswer?: string;
  wasCorrect: boolean;
  mistakesCount?: number;
}

export interface SaveGameResultRequest {
  setId: number;
  mode: GameMode;
  score: number;
  totalQuestions: number;
  durationSeconds?: number;
  questionResults?: SaveGameQuestionResultRequest[];
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
