"use client";

import { useCallback } from "react";

import { getGameAttemptQuestionResults } from "@/api/game-results/gameAttemptsService";
import type { GameQuestionResult } from "@/types/games";

import { useGameAnalyticsQuery } from "./useGameAnalyticsQuery";

export function useGameQuestionResults(
  attemptId: number | null | undefined,
  token: string | null | undefined,
) {
  const isEnabled = Boolean(attemptId && token);

  const queryFn = useCallback(() => {
    if (!attemptId || !token) {
      return Promise.reject(new Error("Attempt ID or token is missing."));
    }

    return getGameAttemptQuestionResults(attemptId, token);
  }, [attemptId, token]);

  return useGameAnalyticsQuery<GameQuestionResult[]>(queryFn, {
    enabled: isEnabled,
    queryKey: `game-question-results:${attemptId ?? "none"}:${token ?? "none"}`,
  });
}
