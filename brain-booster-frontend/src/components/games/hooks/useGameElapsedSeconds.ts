"use client";

import { useEffect, useState } from "react";

export function getElapsedGameSeconds(
  startedAt: number | null | undefined,
  finishedAt?: number | null,
) {
  if (!startedAt) return 0;

  const endTime = finishedAt ?? Date.now();

  return Math.max(0, Math.floor((endTime - startedAt) / 1000));
}

export function formatGameDuration(seconds: number) {
  const safeSeconds = Math.max(0, seconds);
  const minutes = Math.floor(safeSeconds / 60);
  const remainingSeconds = safeSeconds % 60;

  return `${minutes}:${String(remainingSeconds).padStart(2, "0")}`;
}

export function useGameElapsedSeconds(
  startedAt: number | null | undefined,
  finishedAt?: number | null,
) {
  const [currentTime, setCurrentTime] = useState<number | null>(null);

  useEffect(() => {
    if (!startedAt || finishedAt) return;

    const intervalId = window.setInterval(() => {
      setCurrentTime(Date.now());
    }, 1000);

    return () => {
      window.clearInterval(intervalId);
    };
  }, [startedAt, finishedAt]);

  if (!startedAt) return 0;

  const endTime = finishedAt ?? currentTime ?? startedAt;

  return Math.max(0, Math.floor((endTime - startedAt) / 1000));
}
