"use client";

import { useState, type ButtonHTMLAttributes, type CSSProperties } from "react";
import { useRouter } from "next/navigation";
import {
  ArrowLeftRight,
  GripVertical,
  Image as ImageIcon,
  Plus,
  Search,
  SlidersHorizontal,
  Trash2,
  Volume2,
  X,
} from "lucide-react";
import { toast } from "sonner";
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
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Switch } from "@/components/ui/switch";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";

import { useAuth } from "@/context/AuthContext";
import { addFlashcardSet } from "@/api/flashcardSetService";
import { addFlashcard } from "@/api/flashcardService";
import { parseJwt } from "@/utils/jwt";

interface CreateFlashcard {
  flashcardId: string;
  term: string;
  definition: string;
}

export default function CreateSetPage() {
  const router = useRouter();
  const { token } = useAuth();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [isPublic, setIsPublic] = useState(true);
  const [isLoading, setIsLoading] = useState(false);

  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [isClearDialogOpen, setIsClearDialogOpen] = useState(false);

  const [flashcards, setFlashcards] = useState<CreateFlashcard[]>([
    {
      flashcardId: "initial-card-1",
      term: "",
      definition: "",
    },
  ]);

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

  const isFiltering = searchQuery.trim().length > 0;

  const filteredFlashcards = flashcards.filter((flashcard) => {
    const normalizedQuery = searchQuery.trim().toLowerCase();

    if (!normalizedQuery) {
      return true;
    }

    return (
      flashcard.term.toLowerCase().includes(normalizedQuery) ||
      flashcard.definition.toLowerCase().includes(normalizedQuery)
    );
  });

  const getValidFlashcards = () =>
    flashcards.filter(
      (flashcard) => flashcard.term.trim() && flashcard.definition.trim(),
    );

  const isCreateDisabled =
    isLoading || !title.trim() || getValidFlashcards().length < 1;

  const addNewFlashcard = () => {
    setFlashcards((previousFlashcards) => [
      ...previousFlashcards,
      {
        flashcardId: crypto.randomUUID(),
        term: "",
        definition: "",
      },
    ]);
  };

  const removeFlashcard = (id: string) => {
    if (flashcards.length <= 1) {
      return;
    }

    setFlashcards((previousFlashcards) =>
      previousFlashcards.filter((flashcard) => flashcard.flashcardId !== id),
    );
  };

  const updateFlashcard = (
    id: string,
    field: "term" | "definition",
    value: string,
  ) => {
    setFlashcards((previousFlashcards) =>
      previousFlashcards.map((flashcard) =>
        flashcard.flashcardId === id
          ? { ...flashcard, [field]: value }
          : flashcard,
      ),
    );
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (!over || active.id === over.id) {
      return;
    }

    setFlashcards((previousFlashcards) => {
      const oldIndex = previousFlashcards.findIndex(
        (flashcard) => flashcard.flashcardId === active.id,
      );

      const newIndex = previousFlashcards.findIndex(
        (flashcard) => flashcard.flashcardId === over.id,
      );

      if (oldIndex === -1 || newIndex === -1) {
        return previousFlashcards;
      }

      return arrayMove(previousFlashcards, oldIndex, newIndex);
    });
  };

  const handleFlipTermsAndDefinitions = () => {
    setFlashcards((previousFlashcards) =>
      previousFlashcards.map((flashcard) => ({
        ...flashcard,
        term: flashcard.definition,
        definition: flashcard.term,
      })),
    );

    toast.success("Terms and definitions flipped.");
  };

  const handleClearDraft = () => {
    setTitle("");
    setDescription("");
    setFlashcards([
      {
        flashcardId: crypto.randomUUID(),
        term: "",
        definition: "",
      },
    ]);
    setSearchQuery("");
    setIsSearchOpen(false);
    setIsClearDialogOpen(false);
    toast.success("Draft cleared.");
  };

  const handleCreate = async () => {
    if (!token) {
      toast.error("You must be logged in to create a set.");
      return;
    }

    const decodedToken = parseJwt(token);

    if (!decodedToken || !decodedToken.id) {
      toast.error("Invalid session. Please log in again.");
      return;
    }

    const validFlashcards = getValidFlashcards();

    if (!title.trim()) {
      toast.error("Title is required.");
      return;
    }

    if (validFlashcards.length < 1) {
      toast.error("At least one completed flashcard is required.");
      return;
    }

    setIsLoading(true);

    try {
      const createdSet = await addFlashcardSet(
        {
          userId: decodedToken.id,
          setName: title.trim(),
          description: description.trim(),
        },
        token,
      );

      const newSetId = createdSet.setId;

      if (!newSetId) {
        throw new Error("Failed to retrieve the new set ID from server.");
      }

      await Promise.all(
        validFlashcards.map((flashcard) =>
          addFlashcard(
            {
              setId: newSetId,
              term: flashcard.term.trim(),
              definition: flashcard.definition.trim(),
            },
            token,
          ),
        ),
      );

      toast.success("Study set and flashcards created successfully!");
      router.push("/profile");
    } catch (error) {
      const message =
        error instanceof Error ? error.message : "Something went wrong.";

      toast.error(message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <div className="flex min-h-screen flex-col bg-gray-50">
        <header className="sticky top-0 z-10 flex items-center justify-between border-b border-gray-200 bg-white px-4 py-3 shadow-sm md:px-6">
          <div className="flex items-center gap-4">
            <button
              onClick={() => router.back()}
              className="rounded-full p-2 text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-700"
            >
              <X className="h-5 w-5" />
            </button>

            <h1 className="text-lg font-semibold text-gray-800">
              Create a new study set
            </h1>
          </div>

          <div className="flex items-center gap-2">
            <div className="hidden items-center gap-2 lg:flex">
              <Label htmlFor="visibility" className="text-sm text-gray-600">
                Visible to everyone
              </Label>
              <Switch
                id="visibility"
                checked={isPublic}
                onCheckedChange={setIsPublic}
                className="data-[state=checked]:bg-pink-500"
              />
            </div>

            <Button
              type="button"
              variant="ghost"
              size="icon"
              aria-label="Search cards"
              className={cn(
                "h-11 w-11 rounded-full text-gray-500 hover:bg-pink-50 hover:text-pink-500",
                isSearchOpen && "bg-pink-50 text-pink-500",
              )}
              onClick={() => {
                setIsSearchOpen((previousValue) => !previousValue);
                setSearchQuery("");
              }}
            >
              <Search className="h-5 w-5" />
            </Button>

            <Button
              type="button"
              variant="ghost"
              size="icon"
              aria-label="Flip terms and definitions"
              className="h-11 w-11 rounded-full text-gray-500 hover:bg-pink-50 hover:text-pink-500"
              onClick={handleFlipTermsAndDefinitions}
            >
              <ArrowLeftRight className="h-5 w-5" />
            </Button>

            <Button
              type="button"
              variant="ghost"
              size="icon"
              aria-label="Clear draft"
              className="h-11 w-11 rounded-full text-gray-500 hover:bg-red-50 hover:text-red-500"
              onClick={() => setIsClearDialogOpen(true)}
            >
              <Trash2 className="h-5 w-5" />
            </Button>

            <Button
              onClick={handleCreate}
              disabled={isCreateDisabled}
              className="bg-pink-500 text-white hover:bg-pink-600 disabled:bg-gray-300"
            >
              {isLoading ? "Creating..." : "Create"}
            </Button>
          </div>
        </header>

        <main className={cn("flex-1 overflow-y-auto", isSearchOpen && "pb-24")}>
          <div className="mx-auto max-w-4xl px-4 py-6 md:px-6">
            <div className="mb-8 space-y-4">
              <div className="group relative">
                <Input
                  placeholder="Enter a title, like 'Biology - Chapter 22: Evolution'"
                  value={title}
                  onChange={(event) => setTitle(event.target.value)}
                  className="border-0 border-b-2 border-gray-200 bg-transparent px-0 py-3 text-xl font-medium text-gray-800 placeholder:text-gray-400 focus:border-pink-500 focus:ring-0 focus-visible:ring-0"
                />

                <span className="absolute -bottom-5 left-0 text-xs font-medium uppercase tracking-wide text-gray-400">
                  Title
                </span>
              </div>

              <div className="group relative mt-8">
                <Textarea
                  placeholder="Add a description..."
                  value={description}
                  onChange={(event) => setDescription(event.target.value)}
                  className="min-h-[60px] resize-none border-0 border-b-2 border-gray-200 bg-transparent px-0 py-3 text-gray-700 placeholder:text-gray-400 focus:border-pink-500 focus:ring-0 focus-visible:ring-0"
                />

                <span className="absolute -bottom-5 left-0 text-xs font-medium uppercase tracking-wide text-gray-400">
                  Description
                </span>
              </div>
            </div>

            <div className="mb-6 flex flex-wrap gap-3">
              <Button
                variant="outline"
                size="sm"
                className="border-gray-200 text-gray-600 hover:border-pink-300 hover:text-pink-500"
              >
                <Plus className="mr-2 h-4 w-4" />
                Import from Word, Excel, Google Docs, etc.
              </Button>

              <Select defaultValue="both">
                <SelectTrigger className="w-[180px] border-gray-200 text-gray-600">
                  <SelectValue placeholder="Study both sides" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="both">Study both sides</SelectItem>
                  <SelectItem value="term">Term only</SelectItem>
                  <SelectItem value="definition">Definition only</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {isFiltering && (
              <p className="mb-4 text-sm text-gray-500">
                Drag and drop is disabled while searching.
              </p>
            )}

            {isFiltering ? (
              <div className="space-y-4">
                {(isFiltering ? filteredFlashcards : flashcards).map(
                  (flashcard) => {
                    const originalIndex =
                      flashcards.findIndex(
                        (item) => item.flashcardId === flashcard.flashcardId,
                      ) + 1;

                    return (
                      <FlashcardCard
                        key={flashcard.flashcardId}
                        index={originalIndex}
                        flashcard={flashcard}
                        onUpdate={(field, value) =>
                          updateFlashcard(flashcard.flashcardId, field, value)
                        }
                        onRemove={() => removeFlashcard(flashcard.flashcardId)}
                        canRemove={flashcards.length > 1}
                        isDragDisabled
                      />
                    );
                  },
                )}
              </div>
            ) : (
              <DndContext
                id="create-set-dnd-context"
                sensors={sensors}
                collisionDetection={closestCenter}
                onDragEnd={handleDragEnd}
              >
                <SortableContext
                  items={flashcards.map((flashcard) => flashcard.flashcardId)}
                  strategy={verticalListSortingStrategy}
                >
                  <div className="space-y-4">
                    {flashcards.map((flashcard, index) => (
                      <SortableFlashcardCard
                        key={flashcard.flashcardId}
                        id={flashcard.flashcardId}
                        index={index + 1}
                        flashcard={flashcard}
                        onUpdate={(field, value) =>
                          updateFlashcard(flashcard.flashcardId, field, value)
                        }
                        onRemove={() => removeFlashcard(flashcard.flashcardId)}
                        canRemove={flashcards.length > 1}
                      />
                    ))}
                  </div>
                </SortableContext>
              </DndContext>
            )}

            {filteredFlashcards.length === 0 && (
              <div className="rounded-lg border border-gray-200 bg-white py-10 text-center text-gray-500">
                No cards found.
              </div>
            )}

            <button
              onClick={addNewFlashcard}
              className="mt-4 flex w-full items-center justify-center gap-2 rounded-lg border-2 border-dashed border-gray-200 bg-white py-4 text-gray-500 transition-all hover:border-pink-300 hover:bg-pink-50 hover:text-pink-500"
            >
              <Plus className="h-5 w-5" />
              <span className="font-medium">Add card</span>
            </button>

            <div className="mt-8 flex justify-center pb-8">
              <Button
                onClick={handleCreate}
                disabled={isCreateDisabled}
                size="lg"
                className="bg-pink-500 px-12 text-white hover:bg-pink-600 disabled:bg-gray-300"
              >
                {isLoading ? "Creating..." : "Create"}
              </Button>
            </div>
          </div>
        </main>
      </div>

      <div
        className={cn(
          "fixed inset-x-0 bottom-6 z-50 px-4 transition-all duration-300",
          isSearchOpen
            ? "translate-y-0 opacity-100"
            : "pointer-events-none translate-y-8 opacity-0",
        )}
      >
        <div className="mx-auto flex h-14 max-w-2xl items-center gap-3 rounded-full border-2 border-pink-300 bg-white px-4 shadow-[0_10px_35px_rgba(0,0,0,0.15)]">
          <Search className="h-5 w-5 shrink-0 text-gray-500" />

          <Input
            autoFocus={isSearchOpen}
            placeholder="Search"
            value={searchQuery}
            onChange={(event) => setSearchQuery(event.target.value)}
            className="h-full flex-1 border-0 bg-transparent px-0 text-base text-gray-800 placeholder:text-gray-400 focus-visible:ring-0 focus-visible:ring-offset-0"
          />

          <Button
            type="button"
            variant="ghost"
            size="icon"
            aria-label="Search filters"
            className="h-9 w-9 shrink-0 rounded-full text-gray-500 hover:bg-pink-50 hover:text-pink-500"
            onClick={() => toast.info("Search filters coming soon.")}
          >
            <SlidersHorizontal className="h-5 w-5" />
          </Button>

          <Button
            type="button"
            variant="ghost"
            size="icon"
            aria-label="Close search"
            className="h-9 w-9 shrink-0 rounded-full text-gray-500 hover:bg-gray-100 hover:text-gray-700"
            onClick={() => {
              setIsSearchOpen(false);
              setSearchQuery("");
            }}
          >
            <X className="h-5 w-5" />
          </Button>
        </div>
      </div>

      <AlertDialog open={isClearDialogOpen} onOpenChange={setIsClearDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Clear this draft?</AlertDialogTitle>
            <AlertDialogDescription>
              This will remove the current title, description and all cards from
              this draft. This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>

          <AlertDialogFooter>
            <AlertDialogCancel disabled={isLoading}>Cancel</AlertDialogCancel>
            <AlertDialogAction
              disabled={isLoading}
              onClick={handleClearDraft}
              className="bg-red-500 text-white hover:bg-red-600"
            >
              Clear draft
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}

interface SortableFlashcardCardProps {
  id: string;
  index: number;
  flashcard: CreateFlashcard;
  onUpdate: (field: "term" | "definition", value: string) => void;
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
      <FlashcardCard
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

interface FlashcardCardProps {
  index: number;
  flashcard: CreateFlashcard;
  onUpdate: (field: "term" | "definition", value: string) => void;
  onRemove: () => void;
  canRemove: boolean;
  isDragging?: boolean;
  isDragDisabled?: boolean;
  dragHandleProps?: ButtonHTMLAttributes<HTMLButtonElement>;
}

function FlashcardCard({
  index,
  flashcard,
  onUpdate,
  onRemove,
  canRemove,
  isDragging = false,
  isDragDisabled = false,
  dragHandleProps,
}: FlashcardCardProps) {
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
              <button className="rounded p-1 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600">
                <ImageIcon className="h-4 w-4" />
              </button>

              <button className="rounded p-1 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600">
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
              <button className="rounded p-1 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600">
                <ImageIcon className="h-4 w-4" />
              </button>

              <button className="rounded p-1 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600">
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
