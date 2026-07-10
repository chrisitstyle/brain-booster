"use client";

import { Copy, MoreHorizontal, Pencil, Printer, Trash2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

interface StudySetOptionsDropdownProps {
  onEdit: () => void;
  onMakeCopy: () => void;
  onPrint: () => void;
  onDelete: () => void;
}

export default function StudySetOptionsDropdown({
  onEdit,
  onMakeCopy,
  onPrint,
  onDelete,
}: StudySetOptionsDropdownProps) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className="h-10 w-10 rounded-full text-muted-foreground hover:bg-pink-50 hover:text-pink-500 dark:hover:bg-pink-950/40 dark:hover:text-pink-400"
          aria-label="Open set options"
        >
          <MoreHorizontal className="h-5 w-5" aria-hidden="true" />
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent
        align="end"
        className="w-44 border-border bg-popover text-popover-foreground"
      >
        <DropdownMenuItem className="cursor-pointer gap-2" onSelect={onEdit}>
          <Pencil className="h-4 w-4" aria-hidden="true" />
          Edit
        </DropdownMenuItem>

        <DropdownMenuItem
          className="cursor-pointer gap-2"
          onSelect={onMakeCopy}
        >
          <Copy className="h-4 w-4" aria-hidden="true" />
          Make copy
        </DropdownMenuItem>

        <DropdownMenuItem className="cursor-pointer gap-2" onSelect={onPrint}>
          <Printer className="h-4 w-4" aria-hidden="true" />
          Print
        </DropdownMenuItem>

        <DropdownMenuSeparator />

        <DropdownMenuItem
          className="cursor-pointer gap-2 text-destructive focus:bg-destructive/10 focus:text-destructive"
          onSelect={onDelete}
        >
          <Trash2 className="h-4 w-4" aria-hidden="true" />
          Delete
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
