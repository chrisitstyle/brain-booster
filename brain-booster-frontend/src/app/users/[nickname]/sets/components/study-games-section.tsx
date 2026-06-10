"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import {
  FileQuestion,
  Keyboard,
  Layers,
  MousePointerClick,
} from "lucide-react";

import {
  getMyGameResults,
  type GameMode,
  type GameResult,
} from "@/api/gameResultService";
import { useAuth } from "@/context/AuthContext";

interface StudyGamesSectionProps {
  nickname: string;
  setId: number | string;
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

  const [gameResults, setGameResults] = useState<GameResult[]>([]);
  const [isLoadingResults, setIsLoadingResults] = useState(false);

  useEffect(() => {
    if (!token) return;

    const authToken = token;
    let isCancelled = false;

    async function loadGameResults() {
      try {
        setIsLoadingResults(true);

        const results = await getMyGameResults(authToken, setId);

        if (!isCancelled) {
          setGameResults(results);
        }
      } catch (error) {
        console.error("Failed to load game results:", error);
      } finally {
        if (!isCancelled) {
          setIsLoadingResults(false);
        }
      }
    }

    void loadGameResults();

    return () => {
      isCancelled = true;
    };
  }, [token, setId]);

  const resultByMode = useMemo(() => {
    const visibleGameResults = token ? gameResults : [];

    return new Map(visibleGameResults.map((result) => [result.mode, result]));
  }, [token, gameResults]);

  return (
    <section className="mb-8 print:hidden">
      {" "}
      <div className="mx-auto max-w-[550px]">
        {" "}
        <h2 className="mb-3 text-base font-bold text-gray-800">
          Practice modes{" "}
        </h2>
        <div className="grid grid-cols-1 gap-2.5 sm:grid-cols-2 md:grid-cols-3">
          {games.map((game) => {
            const Icon = game.icon;
            const result = resultByMode.get(game.mode);

            return (
              <Link
                key={game.name}
                href={`/users/${nickname}/sets/${setId}/games/${game.href}`}
                className="group flex h-24 flex-col items-center justify-center rounded-xl border border-pink-100 bg-white text-center shadow-sm transition hover:-translate-y-0.5 hover:border-pink-200 hover:bg-pink-50/50 hover:shadow-md"
              >
                <div className="mb-2 flex h-8 w-8 items-center justify-center rounded-lg bg-pink-50 text-pink-500 transition group-hover:bg-pink-500 group-hover:text-white">
                  <Icon className="h-4 w-4" />
                </div>

                <span className="px-2 text-xs font-semibold text-gray-800 transition group-hover:text-pink-600">
                  {game.name}
                </span>

                {token && (
                  <span className="mt-1 rounded-full bg-gray-50 px-2 py-0.5 text-[11px] font-medium text-gray-400 transition group-hover:bg-pink-100 group-hover:text-pink-500">
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
