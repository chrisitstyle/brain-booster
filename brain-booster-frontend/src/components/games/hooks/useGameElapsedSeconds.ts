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
  const [elapsedSeconds, setElapsedSeconds] = useState(() =>
    getElapsedGameSeconds(startedAt, finishedAt),
  );

  useEffect(() => {
    setElapsedSeconds(getElapsedGameSeconds(startedAt, finishedAt));

    if (!startedAt || finishedAt) return;

    const intervalId = window.setInterval(() => {
      setElapsedSeconds(getElapsedGameSeconds(startedAt, finishedAt));
    }, 1000);

    return () => {
      window.clearInterval(intervalId);
    };
  }, [startedAt, finishedAt]);

  return elapsedSeconds;
}
