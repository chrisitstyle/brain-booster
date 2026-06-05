"use client";

import Link from "next/link";
import { useState } from "react";
import { MoreHorizontal, Share2, User } from "lucide-react";
import { toast } from "sonner";

import { cn } from "@/lib/utils";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

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

  const handleShareClick = async () => {
    const setUrl = `${window.location.origin}${setHref}`;

    try {
      await navigator.clipboard.writeText(setUrl);
      toast.success("Set link copied to clipboard");
    } catch {
      toast.error("Failed to copy set link");
    }
  };

  return (
    <Card className="group border-gray-200 bg-white transition-all hover:border-pink-200 hover:shadow-md">
      <CardContent className="p-4">
        <div className="mb-3 flex items-start justify-between gap-3">
          <div className="min-w-0 flex-1">
            <Link
              href={setHref}
              className="line-clamp-1 font-semibold text-gray-800 hover:text-pink-500"
            >
              {set.title}
            </Link>

            {set.description && (
              <p className="mt-1 line-clamp-2 text-sm text-gray-600">
                {set.description}
              </p>
            )}

            <p className="mt-2 text-xs font-medium uppercase tracking-wide text-gray-400">
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
                  variant="ghost"
                  size="icon"
                  className={cn(
                    "h-8 w-8 transition-opacity",
                    isDropdownOpen || isMenuForcedOpen
                      ? "opacity-100"
                      : "opacity-0 group-hover:opacity-100",
                  )}
                >
                  <MoreHorizontal className="h-4 w-4 text-gray-400" />
                </Button>
              </DropdownMenuTrigger>

              <DropdownMenuContent align="end">
                <DropdownMenuItem asChild>
                  <Link href={setHref}>Study</Link>
                </DropdownMenuItem>

                {onAddToFolderClick && (
                  <DropdownMenuItem
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
                  onSelect={(event) => {
                    event.preventDefault();
                    handleShareClick();
                    setIsDropdownOpen(false);
                  }}
                >
                  Share
                </DropdownMenuItem>

                {onDeleteClick && (
                  <DropdownMenuItem
                    className="cursor-pointer text-red-500"
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
              onClick={handleShareClick}
              className="h-8 w-8 opacity-0 transition-opacity group-hover:opacity-100"
            >
              <Share2 className="h-4 w-4 text-gray-400" />
            </Button>
          )}
        </div>

        <div className="flex items-center justify-between gap-3">
          <div className="flex min-w-0 items-center gap-2">
            <Avatar className="h-6 w-6">
              <AvatarFallback className="bg-pink-100 text-xs text-pink-500">
                <User className="h-3 w-3" />
              </AvatarFallback>
            </Avatar>

            <span className="truncate text-sm text-gray-500">
              {set.nickname}
            </span>
          </div>

          {showCreatedAt && set.createdAt && (
            <span className="shrink-0 text-xs text-gray-400">
              {set.createdAt}
            </span>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
