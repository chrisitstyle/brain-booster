import type { ButtonHTMLAttributes } from "react";
import {
  GripVertical,
  Image as ImageIcon,
  Trash2,
  Volume2,
} from "lucide-react";

import { cn } from "@/lib/utils";
import { Textarea } from "@/components/ui/textarea";
import type { FlashcardEditorField, FlashcardEditorItem } from "../types";

interface FlashcardEditorCardProps {
  index: number;
  flashcard: FlashcardEditorItem;
  onUpdate: (field: FlashcardEditorField, value: string) => void;
  onRemove: () => void;
  canRemove: boolean;
  isDragging?: boolean;
  isDragDisabled?: boolean;
  dragHandleProps?: ButtonHTMLAttributes<HTMLButtonElement>;
}

export function FlashcardEditorCard({
  index,
  flashcard,
  onUpdate,
  onRemove,
  canRemove,
  isDragging = false,
  isDragDisabled = false,
  dragHandleProps,
}: FlashcardEditorCardProps) {
  return (
    <div
      className={cn(
        "group overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm transition-all hover:shadow-md",
        isDragging && "scale-[1.01] shadow-xl ring-2 ring-pink-200",
      )}
    >
      <div className="flex items-center justify-between border-b border-gray-100 bg-gray-50 px-4 py-2">
        <div className="flex items-center gap-3">
          <button
            type="button"
            disabled={isDragDisabled}
            aria-label="Drag flashcard"
            {...dragHandleProps}
            className={cn(
              "touch-none rounded p-1 transition-colors",
              isDragDisabled
                ? "cursor-not-allowed text-gray-200"
                : "cursor-grab text-gray-400 hover:bg-gray-200 hover:text-gray-600 active:cursor-grabbing",
            )}
          >
            <GripVertical className="h-4 w-4" />
          </button>

          <span className="font-medium text-gray-600">{index}</span>
        </div>

        <button
          type="button"
          onClick={onRemove}
          disabled={!canRemove}
          className={cn(
            "rounded p-1 transition-colors",
            canRemove
              ? "text-gray-400 hover:bg-gray-200 hover:text-gray-600"
              : "cursor-not-allowed text-gray-200",
          )}
        >
          <Trash2 className="h-4 w-4" />
        </button>
      </div>

      <div className="grid gap-4 p-4 md:grid-cols-2">
        <div className="space-y-2">
          <div className="relative">
            <Textarea
              placeholder="Enter term"
              value={flashcard.term}
              onChange={(event) => onUpdate("term", event.target.value)}
              className="min-h-[80px] resize-none border-0 border-b-2 border-gray-200 bg-transparent px-0 pr-16 text-gray-800 placeholder:text-gray-400 focus:border-pink-500 focus:ring-0 focus-visible:ring-0"
            />

            <div className="absolute bottom-2 right-0 flex gap-2">
              <button
                type="button"
                className="rounded p-1 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
              >
                <ImageIcon className="h-4 w-4" />
              </button>

              <button
                type="button"
                className="rounded p-1 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
              >
                <Volume2 className="h-4 w-4" />
              </button>
            </div>
          </div>

          <span className="text-xs font-medium uppercase tracking-wide text-gray-400">
            Term
          </span>
        </div>

        <div className="space-y-2">
          <div className="relative">
            <Textarea
              placeholder="Enter definition"
              value={flashcard.definition}
              onChange={(event) => onUpdate("definition", event.target.value)}
              className="min-h-[80px] resize-none border-0 border-b-2 border-gray-200 bg-transparent px-0 pr-16 text-gray-800 placeholder:text-gray-400 focus:border-pink-500 focus:ring-0 focus-visible:ring-0"
            />

            <div className="absolute bottom-2 right-0 flex gap-2">
              <button
                type="button"
                className="rounded p-1 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
              >
                <ImageIcon className="h-4 w-4" />
              </button>

              <button
                type="button"
                className="rounded p-1 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
              >
                <Volume2 className="h-4 w-4" />
              </button>
            </div>
          </div>

          <span className="text-xs font-medium uppercase tracking-wide text-gray-400">
            Definition
          </span>
        </div>
      </div>
    </div>
  );
}
