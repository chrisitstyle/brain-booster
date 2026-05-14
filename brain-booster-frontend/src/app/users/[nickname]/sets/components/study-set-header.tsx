"use client";

import Link from "next/link";
import { ChevronLeft } from "lucide-react";

interface StudySetHeaderProps {
  nickname: string;
  setName: string;
  description?: string | null;
}

export default function StudySetHeader({
  nickname,
  setName,
  description,
}: StudySetHeaderProps) {
  return (
    <div className="mb-6">
      <Link
        href={`/users/${nickname}/profile`}
        className="mb-4 inline-flex items-center gap-2 text-gray-500 hover:text-pink-500 print:hidden"
      >
        <ChevronLeft className="h-4 w-4" />
        Back to {nickname}&apos;s profile
      </Link>

      <div>
        <h1 className="text-2xl font-bold text-gray-800 md:text-3xl">
          {setName}
        </h1>

        {description && <p className="mt-2 text-gray-600">{description}</p>}
      </div>
    </div>
  );
}
