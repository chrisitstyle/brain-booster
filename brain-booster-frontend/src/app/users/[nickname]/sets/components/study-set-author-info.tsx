"use client";

import Link from "next/link";

import { Avatar, AvatarFallback } from "@/components/ui/avatar";

interface StudySetAuthorInfoProps {
  nickname: string;
  createdAt: string;
}

export default function StudySetAuthorInfo({
  nickname,
  createdAt,
}: StudySetAuthorInfoProps) {
  const avatarFallback = nickname.trim().charAt(0).toUpperCase() || "?";

  const formattedCreatedAt = new Intl.DateTimeFormat("en-US", {
    month: "long",
    day: "numeric",
    year: "numeric",
  }).format(new Date(createdAt));

  return (
    <div className="mb-8 flex items-center justify-center gap-3 border-t border-gray-200 pt-6">
      <Avatar className="h-10 w-10 border-2 border-pink-200">
        <AvatarFallback className="bg-pink-100 font-medium text-pink-500">
          {avatarFallback}
        </AvatarFallback>
      </Avatar>

      <div className="text-sm">
        <p className="text-gray-500">
          Created by{" "}
          <Link
            href={`/users/${encodeURIComponent(nickname)}/profile`}
            className="font-medium text-pink-500 hover:underline"
          >
            {nickname}
          </Link>
        </p>

        <p className="text-gray-400">Created on {formattedCreatedAt}</p>
      </div>
    </div>
  );
}
