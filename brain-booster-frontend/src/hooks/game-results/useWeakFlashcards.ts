"use client";

import { useCallback } from "react";

import { getMySetWeakFlashcards } from "@/api/game-results/gameAnalyticsService";
import type { WeakFlashcard } from "@/types/games";

import { useGameAnalyticsQuery } from "./useGameAnalyticsQuery";

export function useWeakFlashcards(
  setId: number | null | undefined,
  token: string | null | undefined,
) {
  const queryFn = useCallback(() => {
    if (!setId || !token) {
      return Promise.reject(new Error("Set ID or token is missing."));
    }

    return getMySetWeakFlashcards(setId, token);
  }, [setId, token]);

  return useGameAnalyticsQuery<WeakFlashcard[]>(queryFn, {
    enabled: Boolean(setId && token),
    queryKey: `weak-flashcards:${setId ?? "none"}:${token ?? "none"}`,
  });
}
