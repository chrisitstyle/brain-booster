"use client";

import { useCallback } from "react";

import { getMySetGameAnalyticsSummary } from "@/api/game-results/gameAnalyticsService";
import type { GameAnalyticsSummary } from "@/types/games";

import { useGameAnalyticsQuery } from "./useGameAnalyticsQuery";

export function useGameAnalyticsSummary(
  setId: number | null | undefined,
  token: string | null | undefined,
) {
  const isEnabled = Boolean(setId && token);

  const queryFn = useCallback(() => {
    if (!setId || !token) {
      return Promise.reject(new Error("Set ID or token is missing."));
    }

    return getMySetGameAnalyticsSummary(setId, token);
  }, [setId, token]);

  return useGameAnalyticsQuery<GameAnalyticsSummary>(queryFn, {
    enabled: isEnabled,
    queryKey: `game-analytics-summary:${setId ?? "none"}:${token ?? "none"}`,
  });
}
