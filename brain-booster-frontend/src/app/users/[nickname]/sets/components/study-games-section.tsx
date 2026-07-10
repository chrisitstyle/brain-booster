"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import {
  FileQuestion,
  Keyboard,
  Layers,
  MousePointerClick,
} from "lucide-react";

import { getMyGameResultsBySetId } from "@/api/game-results/gameResultService";
import { useAuth } from "@/context/AuthContext";
import type { GameMode, GameResult } from "@/types/games";

interface StudyGamesSectionProps {
  nickname: string;
  setId: number | string;
}

interface GameResultsState {
  data: GameResult[];
  dataKey: string | null;
  errorKey: string | null;
}

const games: {
  name: string;
  href: string;
  mode: GameMode;
  icon: typeof MousePointerClick;
}[] = [
  {
    name: "Multiple choice",
    href: "quiz",
    mode: "multiple-choice",
    icon: MousePointerClick,
  },
  {
    name: "Written",
    href: "typing",
    mode: "written",
    icon: Keyboard,
  },
  {
    name: "Matching",
    href: "match",
    mode: "matching",
    icon: Layers,
  },
  {
    name: "Custom test",
    href: "test",
    mode: "custom-test",
    icon: FileQuestion,
  },
];

function getResultLabel(result?: GameResult) {
  if (!result) {
    return "Not completed";
  }

  const percentage =
    result.totalQuestions > 0
      ? Math.round((result.score / result.totalQuestions) * 100)
      : 0;

  return `Score: ${result.score} / ${result.totalQuestions} · ${percentage}%`;
}

export default function StudyGamesSection({
  nickname,
  setId,
}: StudyGamesSectionProps) {
  const { token } = useAuth();

  const numericSetId = useMemo(() => {
    const parsedSetId = Number(setId);

    return Number.isFinite(parsedSetId) && parsedSetId > 0 ? parsedSetId : null;
  }, [setId]);

  const queryKey =
    token && numericSetId ? `game-results:${numericSetId}:${token}` : null;

  const [gameResultsState, setGameResultsState] = useState<GameResultsState>({
    data: [],
    dataKey: null,
    errorKey: null,
  });

  const gameResults = useMemo(() => {
    if (!queryKey || gameResultsState.dataKey !== queryKey) {
      return [];
    }

    return gameResultsState.data;
  }, [queryKey, gameResultsState.dataKey, gameResultsState.data]);

  const isLoadingResults = Boolean(
    queryKey &&
    gameResultsState.dataKey !== queryKey &&
    gameResultsState.errorKey !== queryKey,
  );

  useEffect(() => {
    if (!token || !numericSetId || !queryKey) {
      return;
    }

    const authToken = token;
    const currentSetId = numericSetId;
    let isCancelled = false;

    async function loadGameResults() {
      try {
        const results = await getMyGameResultsBySetId(currentSetId, authToken);

        if (isCancelled) {
          return;
        }

        setGameResultsState({
          data: results,
          dataKey: queryKey,
          errorKey: null,
        });
      } catch (error: unknown) {
        if (isCancelled) {
          return;
        }

        console.error("Failed to load game results:", error);

        setGameResultsState((previousState) => ({
          ...previousState,
          errorKey: queryKey,
        }));
      }
    }

    void loadGameResults();

    return () => {
      isCancelled = true;
    };
  }, [token, numericSetId, queryKey]);

  const resultByMode = useMemo(
    () => new Map(gameResults.map((result) => [result.mode, result])),
    [gameResults],
  );

  const encodedNickname = encodeURIComponent(nickname);

  return (
    <section className="mb-8 print:hidden">
      <div className="mx-auto max-w-2xl">
        <h2 className="mb-3 text-base font-bold text-foreground">
          Practice modes
        </h2>

        <div className="grid grid-cols-1 gap-2.5 sm:grid-cols-2">
          {games.map((game) => {
            const Icon = game.icon;
            const result = resultByMode.get(game.mode);

            const gameHref =
              `/users/${encodedNickname}` + `/sets/${setId}/games/${game.href}`;

            return (
              <Link
                key={game.mode}
                href={gameHref}
                className="group flex min-h-24 flex-col items-center justify-center rounded-xl border border-border bg-card px-3 py-4 text-center text-card-foreground shadow-sm transition-all hover:-translate-y-0.5 hover:border-pink-200 hover:bg-pink-50/50 hover:shadow-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring dark:hover:border-pink-900 dark:hover:bg-pink-950/20"
              >
                <div className="mb-2 flex h-8 w-8 items-center justify-center rounded-lg bg-pink-100 text-pink-500 transition-colors group-hover:bg-pink-500 group-hover:text-white dark:bg-pink-950/50 dark:text-pink-400 dark:group-hover:bg-pink-500 dark:group-hover:text-white">
                  <Icon className="h-4 w-4" aria-hidden="true" />
                </div>

                <span className="px-2 text-xs font-semibold text-card-foreground transition-colors group-hover:text-pink-600 dark:group-hover:text-pink-400">
                  {game.name}
                </span>

                {token && (
                  <span
                    className="mt-1 max-w-full truncate rounded-full bg-muted px-2 py-0.5 text-[11px] font-medium text-muted-foreground transition-colors group-hover:bg-pink-100 group-hover:text-pink-500 dark:group-hover:bg-pink-950/60 dark:group-hover:text-pink-400"
                    title={
                      isLoadingResults ? "Loading..." : getResultLabel(result)
                    }
                  >
                    {isLoadingResults ? "Loading..." : getResultLabel(result)}
                  </span>
                )}
              </Link>
            );
          })}
        </div>
      </div>
    </section>
  );
}
