"use client";

import { useState } from "react";
import Link from "next/link";
import { FolderOpen, MoreHorizontal } from "lucide-react";

import type { DashboardFolder } from "@/app/profile/hooks/use-profile-dashboard-data";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { cn } from "@/lib/utils";

interface FolderCardProps {
  readonly folder: DashboardFolder;
  readonly onEditClick: (folder: DashboardFolder) => void;
  readonly onDeleteClick: (folder: DashboardFolder) => void;
  readonly isMenuForcedOpen: boolean;
}

export function FolderCard({
  folder,
  onEditClick,
  onDeleteClick,
  isMenuForcedOpen,
}: FolderCardProps) {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  return (
    <Card className="group border-border bg-card text-card-foreground transition-all hover:border-pink-200 hover:shadow-md dark:hover:border-pink-900">
      <CardContent className="p-4">
        <div className="flex items-start justify-between gap-3">
          <Link
            href={`/profile/folders/${folder.id}`}
            className="flex min-w-0 flex-1 items-center gap-3"
          >
            <div className="rounded-lg bg-pink-100 p-2 dark:bg-pink-950/50">
              <FolderOpen className="h-5 w-5 text-pink-500 dark:text-pink-400" />
            </div>

            <div className="min-w-0">
              <h3 className="line-clamp-1 font-semibold text-card-foreground transition-colors group-hover:text-pink-500 dark:group-hover:text-pink-400">
                {folder.title}
              </h3>

              <p className="text-sm text-muted-foreground">
                {folder.setCount} {folder.setCount === 1 ? "set" : "sets"}
              </p>
            </div>
          </Link>

          <DropdownMenu open={isDropdownOpen} onOpenChange={setIsDropdownOpen}>
            <DropdownMenuTrigger asChild>
              <Button
                type="button"
                variant="ghost"
                size="icon"
                className={cn(
                  "h-8 w-8 shrink-0 transition-opacity",
                  isDropdownOpen || isMenuForcedOpen
                    ? "opacity-100"
                    : "opacity-0 group-hover:opacity-100 group-focus-within:opacity-100",
                )}
                aria-label={`Open options for ${folder.title}`}
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
                  onEditClick(folder);
                  setIsDropdownOpen(false);
                }}
              >
                Edit
              </DropdownMenuItem>

              <DropdownMenuItem
                className="cursor-pointer text-red-500 focus:bg-red-50 focus:text-red-600 dark:focus:bg-red-950/40 dark:focus:text-red-400"
                onSelect={(event) => {
                  event.preventDefault();
                  onDeleteClick(folder);
                  setIsDropdownOpen(false);
                }}
              >
                Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </CardContent>
    </Card>
  );
}
