"use client";

import { useState } from "react";
import Link from "next/link";
import { MoreHorizontal, Share2, User } from "lucide-react";
import { toast } from "sonner";

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

export interface StudySetListItem {
  id: string;
  title: string;
  description: string;
  termCount: number;
  nickname: string;
  createdAt?: string;
}

interface StudySetCardProps {
  set: StudySetListItem;
  showOwnerActions?: boolean;
  showCreatedAt?: boolean;
  isMenuForcedOpen?: boolean;
  onDeleteClick?: (set: StudySetListItem) => void;
  onAddToFolderClick?: (set: StudySetListItem) => void;
}

export default function StudySetCard({
  set,
  showOwnerActions = false,
  showCreatedAt = false,
  isMenuForcedOpen = false,
  onDeleteClick,
  onAddToFolderClick,
}: StudySetCardProps) {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  const setHref = `/users/${encodeURIComponent(set.nickname)}/sets/${set.id}`;

  async function handleShareClick() {
    const setUrl = `${window.location.origin}${setHref}`;

    try {
      await navigator.clipboard.writeText(setUrl);
      toast.success("Set link copied to clipboard.");
    } catch (error: unknown) {
      console.error("Failed to copy set link:", error);

      toast.error("Failed to copy set link.");
    }
  }

  return (
    <Card className="group border-border bg-card text-card-foreground transition-all hover:border-pink-200 hover:shadow-md dark:hover:border-pink-900">
      <CardContent className="p-4">
        <div className="mb-3 flex items-start justify-between gap-3">
          <div className="min-w-0 flex-1">
            <Link
              href={setHref}
              className="line-clamp-1 font-semibold text-card-foreground transition-colors hover:text-pink-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring dark:hover:text-pink-400"
            >
              {set.title}
            </Link>

            {set.description && (
              <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
                {set.description}
              </p>
            )}

            <p className="mt-2 text-xs font-medium uppercase tracking-wide text-muted-foreground">
              {set.termCount} {set.termCount === 1 ? "term" : "terms"}
            </p>
          </div>

          {showOwnerActions ? (
            <DropdownMenu
              open={isDropdownOpen}
              onOpenChange={setIsDropdownOpen}
            >
              <DropdownMenuTrigger asChild>
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  aria-label={`Open actions for ${set.title}`}
                  className={cn(
                    "h-8 w-8 shrink-0 text-muted-foreground transition-opacity hover:bg-accent hover:text-foreground",
                    "opacity-100 md:opacity-0 md:group-hover:opacity-100",
                    (isDropdownOpen || isMenuForcedOpen) && "md:opacity-100",
                  )}
                >
                  <MoreHorizontal className="h-4 w-4" aria-hidden="true" />
                </Button>
              </DropdownMenuTrigger>

              <DropdownMenuContent
                align="end"
                className="border-border bg-popover text-popover-foreground"
              >
                <DropdownMenuItem asChild>
                  <Link href={setHref}>Study</Link>
                </DropdownMenuItem>

                {onAddToFolderClick && (
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
                )}

                <DropdownMenuItem
                  className="cursor-pointer"
                  onSelect={(event) => {
                    event.preventDefault();

                    void handleShareClick();
                    setIsDropdownOpen(false);
                  }}
                >
                  Share
                </DropdownMenuItem>

                {onDeleteClick && (
                  <DropdownMenuItem
                    className="cursor-pointer text-destructive focus:bg-destructive/10 focus:text-destructive"
                    onSelect={(event) => {
                      event.preventDefault();

                      onDeleteClick(set);
                      setIsDropdownOpen(false);
                    }}
                  >
                    Delete
                  </DropdownMenuItem>
                )}
              </DropdownMenuContent>
            </DropdownMenu>
          ) : (
            <Button
              type="button"
              variant="ghost"
              size="icon"
              onClick={() => {
                void handleShareClick();
              }}
              aria-label={`Share ${set.title}`}
              className="h-8 w-8 shrink-0 text-muted-foreground opacity-100 transition-opacity hover:bg-accent hover:text-foreground md:opacity-0 md:group-hover:opacity-100"
            >
              <Share2 className="h-4 w-4" aria-hidden="true" />
            </Button>
          )}
        </div>

        <div className="flex items-center justify-between gap-3">
          <div className="flex min-w-0 items-center gap-2">
            <Avatar className="h-6 w-6">
              <AvatarFallback className="bg-pink-100 text-xs text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
                <User className="h-3 w-3" aria-hidden="true" />
              </AvatarFallback>
            </Avatar>

            <span className="truncate text-sm text-muted-foreground">
              {set.nickname}
            </span>
          </div>

          {showCreatedAt && set.createdAt && (
            <span className="shrink-0 text-xs text-muted-foreground">
              {set.createdAt}
            </span>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
