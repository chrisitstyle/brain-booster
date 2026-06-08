"use client";

import Link from "next/link";
import { ChevronLeft } from "lucide-react";

interface StudySetHeaderProps {
  nickname: string;
  setId: number | string;
  setName: string;
  description?: string | null;
}

export default function StudySetHeader({
  nickname,
  setId,
  setName,
  description,
}: StudySetHeaderProps) {
  return (
    <div className="mb-6">
      <Link
        href={`/users/${nickname}/profile`}
        className="mb-4 inline-flex items-center gap-2 text-gray-500 transition hover:text-pink-500 print:hidden"
      >
        <ChevronLeft className="h-4 w-4" />
        Back to {nickname}&apos;s profile
      </Link>

      <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 md:text-3xl">
            {setName}
          </h1>

          {description && <p className="mt-2 text-gray-600">{description}</p>}
        </div>

        <div className="flex flex-wrap gap-2 print:hidden">
          <Link
            href={`/users/${nickname}/sets/${setId}/games/quiz`}
            className="inline-flex w-fit items-center justify-center rounded-lg bg-pink-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-pink-600"
          >
            Quiz
          </Link>

          <Link
            href={`/users/${nickname}/sets/${setId}/games/typing`}
            className="inline-flex w-fit items-center justify-center rounded-lg border border-pink-200 bg-white px-4 py-2 text-sm font-semibold text-pink-500 transition hover:bg-pink-50"
          >
            Typing
          </Link>
        </div>
      </div>
    </div>
  );
}
