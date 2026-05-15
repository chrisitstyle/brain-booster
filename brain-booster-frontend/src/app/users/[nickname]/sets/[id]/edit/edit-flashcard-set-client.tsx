"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Plus, X } from "lucide-react";
import { toast } from "sonner";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";

import { useAuth } from "@/context/AuthContext";
import {
  addFlashcard,
  deleteFlashcard,
  type Flashcard,
  updateFlashcardById,
} from "@/api/flashcardService";
import {
  deleteFlashcardSet,
  type FlashcardSet,
  updateFlashcardSetById,
} from "@/api/flashcardSetService";

import { useFlashcardEditor } from "@/features/sets/hooks/use-flashcard-editor";
import { BottomSearchBar } from "@/features/sets/components/bottom-search-bar";
import { ConfirmActionDialog } from "@/features/sets/components/confirm-action-dialog";
import { EditorActionButtons } from "@/features/sets/components/editor-action-buttons";
import { SetTitleDescriptionFields } from "@/features/sets/components/set-title-description-fields";
import { SortableFlashcardList } from "@/features/sets/components/sortable-flashcard-list";

interface EditFlashcardSetClientProps {
  setId: string;
  nickname: string;
  initialSet: FlashcardSet;
  initialFlashcards: Flashcard[];
}

export default function EditFlashcardSetClient({
  setId,
  nickname,
  initialSet,
  initialFlashcards,
}: EditFlashcardSetClientProps) {
  const router = useRouter();
  const { token } = useAuth();

  const [title, setTitle] = useState(initialSet.setName);
  const [description, setDescription] = useState(initialSet.description ?? "");
  const [isPublic, setIsPublic] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);

  const flashcardEditor = useFlashcardEditor({
    initialFlashcards:
      initialFlashcards.length > 0
        ? initialFlashcards.map((flashcard) => ({
            localId: flashcard.flashcardId.toString(),
            flashcardId: flashcard.flashcardId,
            term: flashcard.term,
            definition: flashcard.definition,
          }))
        : [
            {
              localId: "initial-card-1",
              term: "",
              definition: "",
            },
          ],
  });

  const isSaveDisabled =
    isLoading || !title.trim() || flashcardEditor.validFlashcards.length < 1;

  const handleFlipTermsAndDefinitions = () => {
    flashcardEditor.flipTermsAndDefinitions();
    toast.success("Terms and definitions flipped.");
  };

  const handleSave = async () => {
    if (!token) {
      toast.error("You must be logged in to edit this set.");
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
      await updateFlashcardSetById(
        setId,
        {
          setName: title.trim(),
          description: description.trim(),
        },
        token,
      );

      const initialFlashcardIds = new Set(
        initialFlashcards.map((flashcard) => flashcard.flashcardId),
      );

      const currentExistingFlashcardIds = new Set(
        flashcardEditor.validFlashcards
          .filter((flashcard) => flashcard.flashcardId)
          .map((flashcard) => flashcard.flashcardId as number),
      );

      const removedFlashcardIds = [...initialFlashcardIds].filter(
        (flashcardId) => !currentExistingFlashcardIds.has(flashcardId),
      );

      await Promise.all([
        ...removedFlashcardIds.map((flashcardId) =>
          deleteFlashcard(flashcardId, token),
        ),

        ...flashcardEditor.validFlashcards.map((flashcard) => {
          if (flashcard.flashcardId) {
            return updateFlashcardById(
              flashcard.flashcardId,
              {
                term: flashcard.term.trim(),
                definition: flashcard.definition.trim(),
              },
              token,
            );
          }

          return addFlashcard(
            {
              setId: Number(setId),
              term: flashcard.term.trim(),
              definition: flashcard.definition.trim(),
            },
            token,
          );
        }),
      ]);

      toast.success("Study set updated successfully.");
      router.push(`/users/${nickname}/sets/${setId}`);
    } catch (error) {
      console.error("Failed to update study set:", error);
      toast.error("Failed to update study set. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteSet = async () => {
    if (!token) {
      toast.error("You must be logged in to delete this set.");
      return;
    }

    setIsLoading(true);

    try {
      await deleteFlashcardSet(setId, token);
      toast.success("Study set deleted successfully.");
      router.push(`/users/${nickname}/profile`);
    } catch (error) {
      console.error("Failed to delete study set:", error);
      toast.error("Failed to delete study set. Please try again.");
    } finally {
      setIsLoading(false);
      setIsDeleteDialogOpen(false);
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
              Edit study set
            </h1>
          </div>

          <EditorActionButtons
            isSearchOpen={flashcardEditor.isSearchOpen}
            isLoading={isLoading}
            isSubmitDisabled={isSaveDisabled}
            submitLabel="Save"
            loadingLabel="Saving..."
            destructiveAriaLabel="Delete study set"
            showVisibilitySwitch
            isPublic={isPublic}
            onPublicChange={setIsPublic}
            onToggleSearch={flashcardEditor.toggleSearch}
            onFlipTermsAndDefinitions={handleFlipTermsAndDefinitions}
            onDestructiveClick={() => setIsDeleteDialogOpen(true)}
            onSubmit={handleSave}
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

            {flashcardEditor.isFiltering && (
              <p className="mb-4 text-sm text-gray-500">
                Drag and drop is disabled while searching.
              </p>
            )}

            <SortableFlashcardList
              dndContextId="edit-set-dnd-context"
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
                onClick={handleSave}
                disabled={isSaveDisabled}
                size="lg"
                className="bg-pink-500 px-12 text-white hover:bg-pink-600 disabled:bg-gray-300"
              >
                {isLoading ? "Saving..." : "Save"}
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
        open={isDeleteDialogOpen}
        title="Delete this study set?"
        description="This action cannot be undone. This will permanently delete this study set and all of its flashcards."
        confirmLabel={isLoading ? "Deleting..." : "Delete"}
        isLoading={isLoading}
        onOpenChange={setIsDeleteDialogOpen}
        onConfirm={handleDeleteSet}
      />
    </>
  );
}
