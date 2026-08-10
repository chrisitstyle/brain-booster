import { type ReactNode } from "react";
import Link from "next/link";
import { Plus, Settings } from "lucide-react";

import type { UserDTO } from "@/api/profileService";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";

interface ProfileHeaderProps {
  readonly currentUser: UserDTO | null;
  readonly isContentLoading: boolean;
}

export function ProfileHeader({
  currentUser,
  isContentLoading,
}: ProfileHeaderProps) {
  const avatarFallback =
    currentUser?.nickname.trim().charAt(0).toUpperCase() || "?";

  const memberSince = currentUser?.createdAt
    ? new Intl.DateTimeFormat("en-US", {
        month: "long",
        year: "numeric",
      }).format(new Date(currentUser.createdAt))
    : "";

  let userContent: ReactNode;

  if (currentUser) {
    userContent = (
      <div className="min-w-0">
        <h1 className="truncate text-2xl font-bold text-foreground">
          {currentUser.nickname}
        </h1>

        <p className="truncate text-muted-foreground">{currentUser.email}</p>

        {memberSince && (
          <p className="mt-1 text-sm text-muted-foreground">
            Member since {memberSince}
          </p>
        )}
      </div>
    );
  } else if (isContentLoading) {
    userContent = (
      <div className="space-y-2">
        <div className="h-7 w-36 animate-pulse rounded bg-muted" />
        <div className="h-5 w-48 animate-pulse rounded bg-muted" />
        <div className="h-4 w-32 animate-pulse rounded bg-muted" />
      </div>
    );
  } else {
    userContent = (
      <div>
        <h1 className="text-xl font-semibold text-foreground">
          Profile unavailable
        </h1>

        <p className="text-sm text-muted-foreground">
          User information could not be loaded.
        </p>
      </div>
    );
  }

  return (
    <div className="mb-8 flex flex-col items-start gap-6 md:flex-row md:items-center md:justify-between">
      <div className="flex min-w-0 items-center gap-4">
        <Avatar className="h-20 w-20 shrink-0 border-4 border-pink-200 dark:border-pink-900">
          <AvatarFallback className="bg-pink-100 text-2xl font-medium text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
            {isContentLoading && !currentUser ? "" : avatarFallback}
          </AvatarFallback>
        </Avatar>

        {userContent}
      </div>

      <div className="flex gap-3">
        <Button
          variant="outline"
          size="sm"
          className="border-border text-muted-foreground hover:border-pink-200 hover:bg-pink-50 hover:text-pink-500 dark:hover:border-pink-900 dark:hover:bg-pink-950/40 dark:hover:text-pink-400"
          asChild
        >
          <Link href="/profile/settings">
            <Settings className="mr-2 h-4 w-4" />
            Settings
          </Link>
        </Button>

        <Button
          size="sm"
          className="bg-pink-500 text-white hover:bg-pink-600"
          asChild
        >
          <Link href="/create-set">
            <Plus className="mr-2 h-4 w-4" />
            Create set
          </Link>
        </Button>
      </div>
    </div>
  );
}
