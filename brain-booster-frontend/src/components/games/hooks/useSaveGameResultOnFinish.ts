"use client";

import { useEffect, useRef, useState } from "react";
import {
  saveGameResult,
  type GameMode,
  type SaveGameResultRequest,
} from "@/api/gameResultService";
import { useAuth } from "@/context/AuthContext";

interface UseSaveGameResultOnFinishParams {
  setId: number | string;
  mode: GameMode;
  score: number;
  totalQuestions: number;
  isFinished: boolean;
  isResultSaved: boolean;
  durationSeconds?: number;
  onSaved: () => void;
}

export function useSaveGameResultOnFinish({
  setId,
  mode,
  score,
  totalQuestions,
  isFinished,
  isResultSaved,
  durationSeconds,
  onSaved,
}: UseSaveGameResultOnFinishParams) {
  const { token } = useAuth();

  const onSavedRef = useRef(onSaved);

  const [isSavingResult, setIsSavingResult] = useState(false);
  const [saveResultError, setSaveResultError] = useState<string | null>(null);

  useEffect(() => {
    onSavedRef.current = onSaved;
  }, [onSaved]);

  useEffect(() => {
    if (!token) return;
    if (!isFinished) return;
    if (isResultSaved) return;
    if (totalQuestions <= 0) return;

    const authToken = token;
    let isCancelled = false;

    async function saveResult() {
      try {
        setIsSavingResult(true);
        setSaveResultError(null);

        const request: SaveGameResultRequest = {
          setId: Number(setId),
          mode,
          score,
          totalQuestions,
          ...(durationSeconds !== undefined ? { durationSeconds } : {}),
        };

        await saveGameResult(request, authToken);

        if (!isCancelled) {
          onSavedRef.current();
        }
      } catch (error) {
        console.error(`Failed to save ${mode} result:`, error);

        if (!isCancelled) {
          setSaveResultError("Failed to save game result.");
        }
      } finally {
        if (!isCancelled) {
          setIsSavingResult(false);
        }
      }
    }

    void saveResult();

    return () => {
      isCancelled = true;
    };
  }, [
    token,
    setId,
    mode,
    score,
    totalQuestions,
    durationSeconds,
    isFinished,
    isResultSaved,
  ]);

  return {
    isSavingResult,
    saveResultError,
  };
}
