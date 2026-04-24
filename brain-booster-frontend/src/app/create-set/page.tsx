"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import {
  GripVertical,
  Plus,
  Trash2,
  X,
  Image as ImageIcon,
  Volume2,
} from "lucide-react";
import { toast } from "sonner";

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

import { useAuth } from "@/context/AuthContext";
import { addFlashcardSet } from "@/api/flashcardSetService";
import { addFlashcard } from "@/api/flashcardService";
import { parseJwt } from "@/utils/jwt";
interface Flashcard {
  flashcardId: string;
  term: string;
  definition: string;
}

export default function CreateSetPage() {
  const router = useRouter();
  const { token } = useAuth(); // get token from context

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [isPublic, setIsPublic] = useState(true);
  const [isLoading, setIsLoading] = useState(false);

  // one flashcard by default, user can add more
  const [flashcards, setFlashcards] = useState<Flashcard[]>([
    { flashcardId: "1", term: "", definition: "" },
  ]);

  const addNewFlashcard = () => {
    setFlashcards([
      ...flashcards,
      { flashcardId: Date.now().toString(), term: "", definition: "" },
    ]);
  };

  const removeFlashcard = (id: string) => {
    if (flashcards.length > 1) {
      setFlashcards(
        flashcards.filter((flashcard) => flashcard.flashcardId !== id),
      );
    }
  };

  const updateFlashcard = (
    id: string,
    field: "term" | "definition",
    value: string,
  ) => {
    setFlashcards(
      flashcards.map((flashcard) =>
        flashcard.flashcardId === id
          ? { ...flashcard, [field]: value }
          : flashcard,
      ),
    );
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

    const validFlashcards = flashcards.filter(
      (f) => f.term.trim() && f.definition.trim(),
    );

    setIsLoading(true);
    try {
      const createdSet = await addFlashcardSet(
        {
          userId: decodedToken.id,
          setName: title,
          description: description,
        },
        token,
      );

      const newSetId = createdSet.flashcardSetId;

      if (!newSetId) {
        throw new Error("Failed to retrieve the new set ID from server.");
      }

      const flashcardPromises = validFlashcards.map((flashcard) =>
        addFlashcard(
          {
            setId: newSetId,
            term: flashcard.term,
            definition: flashcard.definition,
          },
          token,
        ),
      );

      await Promise.all(flashcardPromises);

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
    <div className="min-h-screen flex flex-col bg-gray-50">
      {/* Header */}
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
        <div className="flex items-center gap-3">
          <div className="hidden items-center gap-2 md:flex">
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
            onClick={handleCreate}
            disabled={
              isLoading ||
              !title.trim() ||
              // minimum 1 filled flashcard required
              flashcards.filter((f) => f.term.trim() && f.definition.trim())
                .length < 1
            }
            className="bg-pink-500 text-white hover:bg-pink-600 disabled:bg-gray-300"
          >
            {isLoading ? "Creating..." : "Create"}
          </Button>
        </div>
      </header>

      {/* Content */}
      <main className="flex-1 overflow-y-auto">
        <div className="mx-auto max-w-4xl px-4 py-6 md:px-6">
          {/* Title and Description */}
          <div className="mb-8 space-y-4">
            <div className="group relative">
              <Input
                placeholder="Enter a title, like 'Biology - Chapter 22: Evolution'"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
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
                onChange={(e) => setDescription(e.target.value)}
                className="min-h-[60px] resize-none border-0 border-b-2 border-gray-200 bg-transparent px-0 py-3 text-gray-700 placeholder:text-gray-400 focus:border-pink-500 focus:ring-0 focus-visible:ring-0"
              />
              <span className="absolute -bottom-5 left-0 text-xs font-medium uppercase tracking-wide text-gray-400">
                Description
              </span>
            </div>
          </div>

          {/* Import Options */}
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

          {/* Flashcards */}
          <div className="space-y-4">
            {flashcards.map((flashcard, index) => (
              <FlashcardCard
                key={flashcard.flashcardId}
                index={index + 1}
                flashcard={flashcard}
                onUpdate={(field, value) =>
                  updateFlashcard(flashcard.flashcardId, field, value)
                }
                onRemove={() => removeFlashcard(flashcard.flashcardId)}
                // you can remove if there's more than 1 flashcard to prevent empty set creation
                canRemove={flashcards.length > 1}
              />
            ))}
          </div>

          {/* Add Flashcard Button */}
          <button
            onClick={addNewFlashcard}
            className="mt-4 flex w-full items-center justify-center gap-2 rounded-lg border-2 border-dashed border-gray-200 bg-white py-4 text-gray-500 transition-all hover:border-pink-300 hover:bg-pink-50 hover:text-pink-500"
          >
            <Plus className="h-5 w-5" />
            <span className="font-medium">Add card</span>
          </button>

          {/* Bottom Actions */}
          <div className="mt-8 flex justify-center pb-8">
            <Button
              onClick={handleCreate}
              disabled={
                isLoading ||
                !title.trim() ||
                // minimum 1 filled flashcard required
                flashcards.filter((f) => f.term.trim() && f.definition.trim())
                  .length < 1
              }
              size="lg"
              className="bg-pink-500 px-12 text-white hover:bg-pink-600 disabled:bg-gray-300"
            >
              {isLoading ? "Creating..." : "Create"}
            </Button>
          </div>
        </div>
      </main>
    </div>
  );
}

// ----------------------------------------------------
// Flashcard component
// ----------------------------------------------------

interface FlashcardCardProps {
  index: number;
  flashcard: Flashcard;
  onUpdate: (field: "term" | "definition", value: string) => void;
  onRemove: () => void;
  canRemove: boolean;
}

function FlashcardCard({
  index,
  flashcard,
  onUpdate,
  onRemove,
  canRemove,
}: FlashcardCardProps) {
  return (
    <div className="group overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm transition-all hover:shadow-md">
      <div className="flex items-center justify-between border-b border-gray-100 bg-gray-50 px-4 py-2">
        <div className="flex items-center gap-3">
          <GripVertical className="h-4 w-4 cursor-grab text-gray-400" />
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
              onChange={(e) => onUpdate("term", e.target.value)}
              className="min-h-[80px] resize-none border-0 border-b-2 border-gray-200 bg-transparent px-0 text-gray-800 placeholder:text-gray-400 focus:border-pink-500 focus:ring-0 focus-visible:ring-0"
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
              onChange={(e) => onUpdate("definition", e.target.value)}
              className="min-h-[80px] resize-none border-0 border-b-2 border-gray-200 bg-transparent px-0 text-gray-800 placeholder:text-gray-400 focus:border-pink-500 focus:ring-0 focus-visible:ring-0"
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
