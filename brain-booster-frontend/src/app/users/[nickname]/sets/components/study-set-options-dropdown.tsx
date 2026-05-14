"use client";

import { Copy, MoreHorizontal, Pencil, Printer, Trash2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
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
          variant="ghost"
          size="icon"
          className="h-10 w-10 rounded-full text-gray-400 hover:bg-pink-50 hover:text-pink-500"
        >
          <MoreHorizontal className="h-5 w-5" />
          <span className="sr-only">Open set options</span>
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent align="end" className="w-44">
        <DropdownMenuItem className="cursor-pointer gap-2" onClick={onEdit}>
          <Pencil className="h-4 w-4" />
          Edit
        </DropdownMenuItem>

        <DropdownMenuItem className="cursor-pointer gap-2" onClick={onMakeCopy}>
          <Copy className="h-4 w-4" />
          Make copy
        </DropdownMenuItem>

        <DropdownMenuItem className="cursor-pointer gap-2" onClick={onPrint}>
          <Printer className="h-4 w-4" />
          Print
        </DropdownMenuItem>

        <DropdownMenuItem
          className="cursor-pointer gap-2 text-red-500 focus:text-red-500"
          onClick={onDelete}
        >
          <Trash2 className="h-4 w-4" />
          Delete
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
