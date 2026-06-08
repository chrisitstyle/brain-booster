"use client";

import Link from "next/link";
import {
  FileQuestion,
  Keyboard,
  Layers,
  MousePointerClick,
} from "lucide-react";

interface StudyGamesSectionProps {
  nickname: string;
  setId: number | string;
}

const games = [
  {
    name: "Quiz",
    href: "quiz",
    icon: MousePointerClick,
  },
  {
    name: "Typing",
    href: "typing",
    icon: Keyboard,
  },
  {
    name: "Match",
    href: "match",
    icon: Layers,
  },
  {
    name: "Test",
    href: "test",
    icon: FileQuestion,
  },
];

export default function StudyGamesSection({
  nickname,
  setId,
}: StudyGamesSectionProps) {
  return (
    <section className="mb-8 print:hidden">
      <div className="mx-auto max-w-[550px]">
        <h2 className="mb-3 text-base font-bold text-gray-800">
          Practice modes
        </h2>

        <div className="grid grid-cols-1 gap-2.5 sm:grid-cols-2 md:grid-cols-3">
          {games.map((game) => {
            const Icon = game.icon;

            return (
              <Link
                key={game.name}
                href={`/users/${nickname}/sets/${setId}/games/${game.href}`}
                className="group flex h-20 flex-col items-center justify-center rounded-xl border border-pink-100 bg-white text-center shadow-sm transition hover:-translate-y-0.5 hover:border-pink-200 hover:bg-pink-50/50 hover:shadow-md"
              >
                <div className="mb-2 flex h-8 w-8 items-center justify-center rounded-lg bg-pink-50 text-pink-500 transition group-hover:bg-pink-500 group-hover:text-white">
                  <Icon className="h-4 w-4" />
                </div>

                <span className="text-xs font-semibold text-gray-800 transition group-hover:text-pink-600">
                  {game.name}
                </span>
              </Link>
            );
          })}
        </div>
      </div>
    </section>
  );
}
