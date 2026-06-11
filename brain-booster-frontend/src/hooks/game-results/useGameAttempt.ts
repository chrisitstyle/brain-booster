"use client";

import { useCallback } from "react";

import { getGameAttemptById } from "@/api/game-results/gameAttemptsService";
import type { GameAttempt } from "@/types/games";

import { useGameAnalyticsQuery } from "./useGameAnalyticsQuery";

export function useGameAttempt(
  attemptId: number | null | undefined,
  token: string | null | undefined,
) {
  const isEnabled = Boolean(attemptId && token);

  const queryFn = useCallback(() => {
    if (!attemptId || !token) {
      return Promise.reject(new Error("Attempt ID or token is missing."));
    }

    return getGameAttemptById(attemptId, token);
  }, [attemptId, token]);

  return useGameAnalyticsQuery<GameAttempt>(queryFn, {
    enabled: isEnabled,
    queryKey: `game-attempt:${attemptId ?? "none"}:${token ?? "none"}`,
  });
}
