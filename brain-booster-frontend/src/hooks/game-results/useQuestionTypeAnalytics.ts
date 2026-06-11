"use client";

import { useCallback } from "react";

import { getMySetQuestionTypeAnalytics } from "@/api/game-results/gameAnalyticsService";
import type { QuestionTypeAnalytics } from "@/types/games";

import { useGameAnalyticsQuery } from "./useGameAnalyticsQuery";

export function useQuestionTypeAnalytics(
  setId: number | null | undefined,
  token: string | null | undefined,
) {
  const isEnabled = Boolean(setId && token);

  const queryFn = useCallback(() => {
    if (!setId || !token) {
      return Promise.reject(new Error("Set ID or token is missing."));
    }

    return getMySetQuestionTypeAnalytics(setId, token);
  }, [setId, token]);

  return useGameAnalyticsQuery<QuestionTypeAnalytics[]>(queryFn, {
    enabled: isEnabled,
    queryKey: `question-type-analytics:${setId ?? "none"}:${token ?? "none"}`,
  });
}
