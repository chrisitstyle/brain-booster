import Link from "next/link";

import { Avatar, AvatarFallback } from "@/components/ui/avatar";

interface StudySetAuthorInfoProps {
  nickname: string;
  createdAt: string;
}

function formatCreatedAt(createdAt: string) {
  const date = new Date(createdAt);

  if (Number.isNaN(date.getTime())) {
    return "Unknown date";
  }

  return new Intl.DateTimeFormat("en-US", {
    month: "long",
    day: "numeric",
    year: "numeric",
  }).format(date);
}

export default function StudySetAuthorInfo({
  nickname,
  createdAt,
}: StudySetAuthorInfoProps) {
  const avatarFallback = nickname.trim().charAt(0).toUpperCase() || "?";

  const formattedCreatedAt = formatCreatedAt(createdAt);

  const profileHref = `/users/${encodeURIComponent(nickname)}/profile`;

  return (
    <div className="mb-8 flex items-center justify-center gap-3 border-t border-border pt-6">
      <Avatar className="h-10 w-10 border-2 border-pink-200 dark:border-pink-900">
        <AvatarFallback className="bg-pink-100 font-medium text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
          {avatarFallback}
        </AvatarFallback>
      </Avatar>

      <div className="min-w-0 text-sm">
        <p className="text-muted-foreground">
          Created by{" "}
          <Link
            href={profileHref}
            className="font-medium text-pink-500 transition-colors hover:text-pink-600 hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring dark:text-pink-400 dark:hover:text-pink-300"
          >
            {nickname}
          </Link>
        </p>

        <p className="text-muted-foreground">
          Created on <time dateTime={createdAt}>{formattedCreatedAt}</time>
        </p>
      </div>
    </div>
  );
}
