import type { ButtonHTMLAttributes } from "react";
import {
  GripVertical,
  Image as ImageIcon,
  Trash2,
  Volume2,
} from "lucide-react";

import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { cn } from "@/lib/utils";

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
  const termId = `flashcard-${flashcard.localId}-term`;
  const definitionId = `flashcard-${flashcard.localId}-definition`;

  return (
    <div
      className={cn(
        "group overflow-hidden rounded-lg border border-border bg-card text-card-foreground shadow-sm transition-all hover:border-pink-200 hover:shadow-md dark:hover:border-pink-900",
        isDragging &&
          "scale-[1.01] border-pink-300 shadow-xl ring-2 ring-pink-500/30 dark:border-pink-800",
      )}
    >
      <div className="flex items-center justify-between border-b border-border bg-muted/50 px-4 py-2">
        <div className="flex items-center gap-3">
          <button
            type="button"
            {...dragHandleProps}
            disabled={isDragDisabled}
            aria-label={`Drag flashcard ${index}`}
            className={cn(
              "touch-none rounded p-1 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring",
              isDragDisabled
                ? "cursor-not-allowed text-muted-foreground/30"
                : "cursor-grab text-muted-foreground hover:bg-accent hover:text-foreground active:cursor-grabbing",
            )}
          >
            <GripVertical className="h-4 w-4" aria-hidden="true" />
          </button>

          <span className="font-medium text-muted-foreground">{index}</span>
        </div>

        <button
          type="button"
          onClick={onRemove}
          disabled={!canRemove}
          aria-label={`Remove flashcard ${index}`}
          className={cn(
            "rounded p-1 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring",
            canRemove
              ? "text-muted-foreground hover:bg-red-50 hover:text-red-500 dark:hover:bg-red-950/40 dark:hover:text-red-400"
              : "cursor-not-allowed text-muted-foreground/30",
          )}
        >
          <Trash2 className="h-4 w-4" aria-hidden="true" />
        </button>
      </div>

      <div className="grid gap-6 p-4 md:grid-cols-2">
        <div className="space-y-2">
          <div className="relative">
            <Textarea
              id={termId}
              placeholder="Enter term"
              value={flashcard.term}
              onChange={(event) => onUpdate("term", event.target.value)}
              className="min-h-[80px] resize-none rounded-none border-0 border-b-2 border-border bg-transparent px-0 pr-16 text-foreground shadow-none placeholder:text-muted-foreground focus-visible:border-pink-500 focus-visible:ring-0 dark:focus-visible:border-pink-400"
            />

            <div className="absolute bottom-2 right-0 flex gap-1">
              <button
                type="button"
                className="rounded p-1 text-muted-foreground transition-colors hover:bg-accent hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                aria-label={`Add image to term in flashcard ${index}`}
              >
                <ImageIcon className="h-4 w-4" aria-hidden="true" />
              </button>

              <button
                type="button"
                className="rounded p-1 text-muted-foreground transition-colors hover:bg-accent hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                aria-label={`Read term aloud in flashcard ${index}`}
              >
                <Volume2 className="h-4 w-4" aria-hidden="true" />
              </button>
            </div>
          </div>

          <Label
            htmlFor={termId}
            className="text-xs font-medium uppercase tracking-wide text-muted-foreground"
          >
            Term
          </Label>
        </div>

        <div className="space-y-2">
          <div className="relative">
            <Textarea
              id={definitionId}
              placeholder="Enter definition"
              value={flashcard.definition}
              onChange={(event) => onUpdate("definition", event.target.value)}
              className="min-h-[80px] resize-none rounded-none border-0 border-b-2 border-border bg-transparent px-0 pr-16 text-foreground shadow-none placeholder:text-muted-foreground focus-visible:border-pink-500 focus-visible:ring-0 dark:focus-visible:border-pink-400"
            />

            <div className="absolute bottom-2 right-0 flex gap-1">
              <button
                type="button"
                className="rounded p-1 text-muted-foreground transition-colors hover:bg-accent hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                aria-label={`Add image to definition in flashcard ${index}`}
              >
                <ImageIcon className="h-4 w-4" aria-hidden="true" />
              </button>

              <button
                type="button"
                className="rounded p-1 text-muted-foreground transition-colors hover:bg-accent hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                aria-label={`Read definition aloud in flashcard ${index}`}
              >
                <Volume2 className="h-4 w-4" aria-hidden="true" />
              </button>
            </div>
          </div>

          <Label
            htmlFor={definitionId}
            className="text-xs font-medium uppercase tracking-wide text-muted-foreground"
          >
            Definition
          </Label>
        </div>
      </div>
    </div>
  );
}
