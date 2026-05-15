"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Plus, X } from "lucide-react";
import { toast } from "sonner";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
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

import { useFlashcardEditor } from "@/features/sets/hooks/use-flashcard-editor";
import { BottomSearchBar } from "@/features/sets/components/bottom-search-bar";
import { ConfirmActionDialog } from "@/features/sets/components/confirm-action-dialog";
import { EditorActionButtons } from "@/features/sets/components/editor-action-buttons";
import { SetTitleDescriptionFields } from "@/features/sets/components/set-title-description-fields";
import { SortableFlashcardList } from "@/features/sets/components/sortable-flashcard-list";

export default function CreateSetPage() {
  const router = useRouter();
  const { token } = useAuth();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [isPublic, setIsPublic] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [isClearDialogOpen, setIsClearDialogOpen] = useState(false);

  const flashcardEditor = useFlashcardEditor({
    initialFlashcards: [
      {
        localId: "initial-card-1",
        term: "",
        definition: "",
      },
    ],
  });

  const isCreateDisabled =
    isLoading || !title.trim() || flashcardEditor.validFlashcards.length < 1;

  const handleFlipTermsAndDefinitions = () => {
    flashcardEditor.flipTermsAndDefinitions();
    toast.success("Terms and definitions flipped.");
  };

  const handleClearDraft = () => {
    setTitle("");
    setDescription("");
    flashcardEditor.resetFlashcards();
    flashcardEditor.closeSearch();
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

    if (!title.trim()) {
      toast.error("Title is required.");
      return;
    }

    if (flashcardEditor.validFlashcards.length < 1) {
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
        flashcardEditor.validFlashcards.map((flashcard) =>
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
              type="button"
              onClick={() => router.back()}
              className="rounded-full p-2 text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-700"
            >
              <X className="h-5 w-5" />
            </button>

            <h1 className="text-lg font-semibold text-gray-800">
              Create a new study set
            </h1>
          </div>

          <EditorActionButtons
            isSearchOpen={flashcardEditor.isSearchOpen}
            isLoading={isLoading}
            isSubmitDisabled={isCreateDisabled}
            submitLabel="Create"
            loadingLabel="Creating..."
            destructiveAriaLabel="Clear draft"
            showVisibilitySwitch
            isPublic={isPublic}
            onPublicChange={setIsPublic}
            onToggleSearch={flashcardEditor.toggleSearch}
            onFlipTermsAndDefinitions={handleFlipTermsAndDefinitions}
            onDestructiveClick={() => setIsClearDialogOpen(true)}
            onSubmit={handleCreate}
          />
        </header>

        <main
          className={cn(
            "flex-1 overflow-y-auto",
            flashcardEditor.isSearchOpen && "pb-24",
          )}
        >
          <div className="mx-auto max-w-4xl px-4 py-6 md:px-6">
            <SetTitleDescriptionFields
              title={title}
              description={description}
              onTitleChange={setTitle}
              onDescriptionChange={setDescription}
            />

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

            {flashcardEditor.isFiltering && (
              <p className="mb-4 text-sm text-gray-500">
                Drag and drop is disabled while searching.
              </p>
            )}

            <SortableFlashcardList
              dndContextId="create-set-dnd-context"
              flashcards={flashcardEditor.flashcards}
              filteredFlashcards={flashcardEditor.filteredFlashcards}
              isFiltering={flashcardEditor.isFiltering}
              onUpdate={flashcardEditor.updateFlashcard}
              onRemove={flashcardEditor.removeFlashcard}
              onReorder={flashcardEditor.reorderFlashcards}
            />

            {flashcardEditor.filteredFlashcards.length === 0 && (
              <div className="rounded-lg border border-gray-200 bg-white py-10 text-center text-gray-500">
                No cards found.
              </div>
            )}

            <button
              type="button"
              onClick={flashcardEditor.addEmptyFlashcard}
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

      <BottomSearchBar
        isOpen={flashcardEditor.isSearchOpen}
        searchQuery={flashcardEditor.searchQuery}
        onSearchQueryChange={flashcardEditor.setSearchQuery}
        onClose={flashcardEditor.closeSearch}
      />

      <ConfirmActionDialog
        open={isClearDialogOpen}
        title="Clear this draft?"
        description="This will remove the current title, description and all cards from this draft. This action cannot be undone."
        confirmLabel="Clear draft"
        isLoading={isLoading}
        onOpenChange={setIsClearDialogOpen}
        onConfirm={handleClearDraft}
      />
    </>
  );
}
