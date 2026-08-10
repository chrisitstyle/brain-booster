import { apiRequest } from "@/api/apiClient";
import type {
  GameAttempt,
  GameAttemptFilters,
  GameQuestionResult,
  PageResponse,
} from "@/types/games";

export async function getMyGameAttempts(
  token: string,
  filters: GameAttemptFilters = {},
) {
  return apiRequest<PageResponse<GameAttempt>>("/game-attempts/me", {
    token,
    query: {
      page: filters.page,
      size: filters.size,
      setId: filters.setId,
      mode: filters.mode,
      from: filters.from,
      to: filters.to,
    },
  });
}

export async function getMyGameAttemptsBySetId(
  setId: number,
  token: string,
  filters: Omit<GameAttemptFilters, "setId"> = {},
) {
  return apiRequest<PageResponse<GameAttempt>>(
    `/game-attempts/me/sets/${setId}`,
    {
      token,
      query: {
        page: filters.page,
        size: filters.size,
        mode: filters.mode,
        from: filters.from,
        to: filters.to,
      },
    },
  );
}

export async function getGameAttemptById(attemptId: number, token: string) {
  return apiRequest<GameAttempt>(`/game-attempts/${attemptId}`, { token });
}

export async function getGameAttemptQuestionResults(
  attemptId: number,
  token: string,
) {
  return apiRequest<GameQuestionResult[]>(
    `/game-attempts/${attemptId}/question-results`,
    { token },
  );
}

export type {
  GameAttempt,
  GameAttemptFilters,
  GameQuestionResult,
  PageResponse,
};
