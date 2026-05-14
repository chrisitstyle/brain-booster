"use client";

import Link from "next/link";

import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";

interface StudySetAuthorInfoProps {
  nickname: string;
  createdAt: string;
}

export default function StudySetAuthorInfo({
  nickname,
  createdAt,
}: StudySetAuthorInfoProps) {
  return (
    <div className="mb-8 flex items-center justify-center gap-3 border-t border-gray-200 pt-6">
      <Avatar className="h-10 w-10">
        <AvatarImage src="/placeholder.svg?height=40&width=40" />
        <AvatarFallback className="bg-pink-100 text-pink-500">
          {nickname.substring(0, 1).toUpperCase()}
        </AvatarFallback>
      </Avatar>

      <div className="text-sm">
        <p className="text-gray-500">
          Created by{" "}
          <Link
            href={`/users/${nickname}/profile`}
            className="font-medium text-pink-500 hover:underline"
          >
            {nickname}
          </Link>
        </p>

        <p className="text-gray-400">
          Created on {new Date(createdAt).toLocaleDateString("en-US")}
        </p>
      </div>
    </div>
  );
}
