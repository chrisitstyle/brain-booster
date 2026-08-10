"use client";

import { useState } from "react";
import Link from "next/link";
import { MoreHorizontal } from "lucide-react";

import type { StudySet } from "@/app/profile/hooks/use-profile-dashboard-data";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { cn } from "@/lib/utils";

interface StudySetCardProps {
  readonly set: StudySet;
  readonly onEditClick: (set: StudySet) => void;
  readonly onDeleteClick: (id: string) => void;
  readonly onAddToFolderClick: (set: StudySet) => void;
  readonly isDeleteDialogOpen: boolean;
}

export function StudySetCard({
  set,
  onEditClick,
  onDeleteClick,
  onAddToFolderClick,
  isDeleteDialogOpen,
}: StudySetCardProps) {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  const authorInitial = set.author.trim().charAt(0).toUpperCase() || "?";

  return (
    <Card className="group border-border bg-card text-card-foreground transition-all hover:border-pink-200 hover:shadow-md dark:hover:border-pink-900">
      <CardContent className="p-4">
        <div className="mb-3 flex items-start justify-between gap-3">
          <div className="min-w-0 flex-1">
            <Link
              href={`/users/${encodeURIComponent(set.author)}/sets/${set.id}`}
              className="line-clamp-1 font-semibold text-card-foreground transition-colors hover:text-pink-500 dark:hover:text-pink-400"
            >
              {set.title}
            </Link>

            {set.description && (
              <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
                {set.description}
              </p>
            )}

            <p className="mt-2 text-xs font-medium uppercase tracking-wide text-muted-foreground">
              {set.termCount} terms
            </p>
          </div>

          <DropdownMenu open={isDropdownOpen} onOpenChange={setIsDropdownOpen}>
            <DropdownMenuTrigger asChild>
              <Button
                type="button"
                variant="ghost"
                size="icon"
                className={cn(
                  "h-8 w-8 shrink-0 transition-opacity",
                  isDropdownOpen || isDeleteDialogOpen
                    ? "opacity-100"
                    : "opacity-0 group-hover:opacity-100 group-focus-within:opacity-100",
                )}
                aria-label={`Open options for ${set.title}`}
              >
                <MoreHorizontal className="h-4 w-4 text-muted-foreground" />
              </Button>
            </DropdownMenuTrigger>

            <DropdownMenuContent
              align="end"
              className="border-border bg-popover text-popover-foreground"
            >
              <DropdownMenuItem
                className="cursor-pointer"
                onSelect={(event) => {
                  event.preventDefault();
                  onEditClick(set);
                  setIsDropdownOpen(false);
                }}
              >
                Edit
              </DropdownMenuItem>

              <DropdownMenuItem>Share</DropdownMenuItem>

              <DropdownMenuItem
                className="cursor-pointer"
                onSelect={(event) => {
                  event.preventDefault();
                  onAddToFolderClick(set);
                  setIsDropdownOpen(false);
                }}
              >
                Add to folder
              </DropdownMenuItem>

              <DropdownMenuItem
                className="cursor-pointer text-red-500 focus:bg-red-50 focus:text-red-600 dark:focus:bg-red-950/40 dark:focus:text-red-400"
                onSelect={(event) => {
                  event.preventDefault();
                  onDeleteClick(set.id);
                  setIsDropdownOpen(false);
                }}
              >
                Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>

        <div className="flex items-center gap-2">
          <Avatar className="h-6 w-6">
            <AvatarFallback className="bg-pink-100 text-xs text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
              {authorInitial}
            </AvatarFallback>
          </Avatar>

          <span className="truncate text-sm text-muted-foreground">
            {set.author}
          </span>
        </div>
      </CardContent>
    </Card>
  );
}
