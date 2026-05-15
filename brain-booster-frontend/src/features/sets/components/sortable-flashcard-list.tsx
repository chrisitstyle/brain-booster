import type { CSSProperties } from "react";
import {
  closestCenter,
  DndContext,
  KeyboardSensor,
  PointerSensor,
  type DragEndEvent,
  useSensor,
  useSensors,
} from "@dnd-kit/core";
import {
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";

import { FlashcardEditorCard } from "./flashcard-editor-card";
import type { FlashcardEditorField, FlashcardEditorItem } from "../types";

interface SortableFlashcardListProps {
  dndContextId: string;
  flashcards: FlashcardEditorItem[];
  filteredFlashcards: FlashcardEditorItem[];
  isFiltering: boolean;
  onUpdate: (
    localId: string,
    field: FlashcardEditorField,
    value: string,
  ) => void;
  onRemove: (localId: string) => void;
  onReorder: (activeId: string, overId: string) => void;
}

export function SortableFlashcardList({
  dndContextId,
  flashcards,
  filteredFlashcards,
  isFiltering,
  onUpdate,
  onRemove,
  onReorder,
}: SortableFlashcardListProps) {
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    }),
  );

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (!over || active.id === over.id) {
      return;
    }

    onReorder(active.id.toString(), over.id.toString());
  };

  if (isFiltering) {
    return (
      <div className="space-y-4">
        {filteredFlashcards.map((flashcard) => {
          const originalIndex =
            flashcards.findIndex((item) => item.localId === flashcard.localId) +
            1;

          return (
            <FlashcardEditorCard
              key={flashcard.localId}
              index={originalIndex}
              flashcard={flashcard}
              onUpdate={(field, value) =>
                onUpdate(flashcard.localId, field, value)
              }
              onRemove={() => onRemove(flashcard.localId)}
              canRemove={flashcards.length > 1}
              isDragDisabled
            />
          );
        })}
      </div>
    );
  }

  return (
    <DndContext
      id={dndContextId}
      sensors={sensors}
      collisionDetection={closestCenter}
      onDragEnd={handleDragEnd}
    >
      <SortableContext
        items={flashcards.map((flashcard) => flashcard.localId)}
        strategy={verticalListSortingStrategy}
      >
        <div className="space-y-4">
          {flashcards.map((flashcard, index) => (
            <SortableFlashcardCard
              key={flashcard.localId}
              id={flashcard.localId}
              index={index + 1}
              flashcard={flashcard}
              onUpdate={(field, value) =>
                onUpdate(flashcard.localId, field, value)
              }
              onRemove={() => onRemove(flashcard.localId)}
              canRemove={flashcards.length > 1}
            />
          ))}
        </div>
      </SortableContext>
    </DndContext>
  );
}

interface SortableFlashcardCardProps {
  id: string;
  index: number;
  flashcard: FlashcardEditorItem;
  onUpdate: (field: FlashcardEditorField, value: string) => void;
  onRemove: () => void;
  canRemove: boolean;
}

function SortableFlashcardCard({
  id,
  index,
  flashcard,
  onUpdate,
  onRemove,
  canRemove,
}: SortableFlashcardCardProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id });

  const style: CSSProperties = {
    transform: CSS.Transform.toString(transform),
    transition,
    zIndex: isDragging ? 50 : undefined,
    position: "relative",
  };

  return (
    <div ref={setNodeRef} style={style}>
      <FlashcardEditorCard
        index={index}
        flashcard={flashcard}
        onUpdate={onUpdate}
        onRemove={onRemove}
        canRemove={canRemove}
        isDragging={isDragging}
        dragHandleProps={{
          ...attributes,
          ...listeners,
        }}
      />
    </div>
  );
}
