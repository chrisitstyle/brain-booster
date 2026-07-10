"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Plus, X } from "lucide-react";
import { toast } from "sonner";

import { addFlashcard } from "@/api/flashcardService";
import { addFlashcardSet } from "@/api/flashcardSetService";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useAuth } from "@/context/AuthContext";
import { BottomSearchBar } from "@/features/sets/components/bottom-search-bar";
import { ConfirmActionDialog } from "@/features/sets/components/confirm-action-dialog";
import { EditorActionButtons } from "@/features/sets/components/editor-action-buttons";
import { ImportFlashcardsDialog } from "@/features/sets/components/import-flashcards-dialog";
import { SetTitleDescriptionFields } from "@/features/sets/components/set-title-description-fields";
import { SortableFlashcardList } from "@/features/sets/components/sortable-flashcard-list";
import { useFlashcardEditor } from "@/features/sets/hooks/use-flashcard-editor";
import { cn } from "@/lib/utils";

export default function CreateSetPage() {
  const router = useRouter();
  const { token } = useAuth();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [isPublic, setIsPublic] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [isClearDialogOpen, setIsClearDialogOpen] = useState(false);
  const [isImportDialogOpen, setIsImportDialogOpen] = useState(false);

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

  function handleFlipTermsAndDefinitions() {
    flashcardEditor.flipTermsAndDefinitions();
    toast.success("Terms and definitions flipped.");
  }

  function handleClearDraft() {
    setTitle("");
    setDescription("");
    flashcardEditor.resetFlashcards();
    flashcardEditor.closeSearch();
    setIsClearDialogOpen(false);

    toast.success("Draft cleared.");
  }

  async function handleCreate() {
    if (!token) {
      toast.error("You must be logged in to create a set.");
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

    try {
      setIsLoading(true);

      const createdSet = await addFlashcardSet(
        {
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
    } catch (error: unknown) {
      console.error("Failed to create study set:", error);

      const message =
        error instanceof Error ? error.message : "Something went wrong.";

      toast.error(message);
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <>
      <div className="flex min-h-[calc(100svh-4rem)] flex-col bg-background text-foreground">
        <header className="sticky top-16 z-10 flex items-center justify-between gap-4 border-b border-border bg-background/95 px-4 py-3 shadow-sm backdrop-blur supports-[backdrop-filter]:bg-background/80 md:px-6">
          <div className="flex min-w-0 items-center gap-3 sm:gap-4">
            <Button
              type="button"
              variant="ghost"
              size="icon"
              onClick={() => router.back()}
              className="shrink-0 text-muted-foreground hover:bg-accent hover:text-foreground"
              aria-label="Close set creator"
            >
              <X className="h-5 w-5" aria-hidden="true" />
            </Button>

            <h1 className="truncate text-base font-semibold text-foreground sm:text-lg">
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
            onSubmit={() => {
              void handleCreate();
            }}
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
                type="button"
                variant="outline"
                size="sm"
                className="border-border text-muted-foreground hover:border-pink-300 hover:bg-pink-50 hover:text-pink-500 dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-400"
                onClick={() => setIsImportDialogOpen(true)}
              >
                <Plus className="mr-2 h-4 w-4" />
                Import
              </Button>

              <Select defaultValue="both">
                <SelectTrigger className="w-[180px] border-input bg-background text-foreground">
                  <SelectValue placeholder="Study both sides" />
                </SelectTrigger>

                <SelectContent className="border-border bg-popover text-popover-foreground">
                  <SelectItem value="both">Study both sides</SelectItem>

                  <SelectItem value="term">Term only</SelectItem>

                  <SelectItem value="definition">Definition only</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {flashcardEditor.isFiltering && (
              <p className="mb-4 text-sm text-muted-foreground">
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
              <div className="rounded-lg border border-border bg-card py-10 text-center text-muted-foreground">
                No cards found.
              </div>
            )}

            <button
              type="button"
              onClick={flashcardEditor.addEmptyFlashcard}
              className="mt-4 flex w-full items-center justify-center gap-2 rounded-lg border-2 border-dashed border-border bg-card py-4 text-muted-foreground transition-all hover:border-pink-300 hover:bg-pink-50 hover:text-pink-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring dark:hover:border-pink-900 dark:hover:bg-pink-950/30 dark:hover:text-pink-400"
            >
              <Plus className="h-5 w-5" aria-hidden="true" />
              <span className="font-medium">Add card</span>
            </button>

            <div className="mt-8 flex justify-center pb-8">
              <Button
                type="button"
                onClick={() => {
                  void handleCreate();
                }}
                disabled={isCreateDisabled}
                size="lg"
                className="bg-pink-500 px-12 text-white hover:bg-pink-600 disabled:bg-muted disabled:text-muted-foreground"
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

      <ImportFlashcardsDialog
        open={isImportDialogOpen}
        onOpenChange={setIsImportDialogOpen}
        onImport={flashcardEditor.appendImportedFlashcards}
        allowCsvImport
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
