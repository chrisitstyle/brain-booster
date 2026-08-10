import Link from "next/link";
import { BookOpen } from "lucide-react";

import type { StudySet } from "@/app/profile/hooks/use-profile-dashboard-data";
import { Button } from "@/components/ui/button";

interface RecentActivityItemProps {
  readonly set: StudySet;
}

export function RecentActivityItem({ set }: RecentActivityItemProps) {
  return (
    <div className="flex flex-col gap-4 rounded-lg border border-border bg-card p-4 text-card-foreground transition-all hover:border-pink-200 hover:shadow-sm dark:hover:border-pink-900 sm:flex-row sm:items-center sm:justify-between">
      <div className="flex min-w-0 items-center gap-4">
        <div className="shrink-0 rounded-lg bg-pink-100 p-2 dark:bg-pink-950/50">
          <BookOpen className="h-5 w-5 text-pink-500 dark:text-pink-400" />
        </div>

        <div className="min-w-0">
          <Link
            href={`/users/${encodeURIComponent(set.author)}/sets/${set.id}`}
            className="line-clamp-1 font-medium text-card-foreground transition-colors hover:text-pink-500 dark:hover:text-pink-400"
          >
            {set.title}
          </Link>

          <p className="text-sm text-muted-foreground">{set.termCount} terms</p>
        </div>
      </div>

      <div className="flex items-center justify-between gap-4 sm:justify-end">
        <span className="text-sm text-muted-foreground">{set.lastStudied}</span>

        <Button
          size="sm"
          className="bg-pink-500 text-white hover:bg-pink-600"
          asChild
        >
          <Link
            href={`/users/${encodeURIComponent(set.author)}/sets/${set.id}`}
          >
            Study
          </Link>
        </Button>
      </div>
    </div>
  );
}
