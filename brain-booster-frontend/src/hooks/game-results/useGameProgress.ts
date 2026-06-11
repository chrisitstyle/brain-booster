"use client";

import { useCallback } from "react";

import { getMySetGameProgress } from "@/api/game-results/gameAnalyticsService";
import type { GameProgressPoint } from "@/types/games";

import { useGameAnalyticsQuery } from "./useGameAnalyticsQuery";

export function useGameProgress(
  setId: number | null | undefined,
  token: string | null | undefined,
) {
  const isEnabled = Boolean(setId && token);

  const queryFn = useCallback(() => {
    if (!setId || !token) {
      return Promise.reject(new Error("Set ID or token is missing."));
    }

    return getMySetGameProgress(setId, token);
  }, [setId, token]);

  return useGameAnalyticsQuery<GameProgressPoint[]>(queryFn, {
    enabled: isEnabled,
    queryKey: `game-progress:${setId ?? "none"}:${token ?? "none"}`,
  });
}
