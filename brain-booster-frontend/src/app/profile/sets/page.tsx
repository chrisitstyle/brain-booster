"use client";

import { useEffect, useMemo, useState, type MouseEvent } from "react";
import Link from "next/link";
import { Loader2, Plus, Search } from "lucide-react";
import { toast } from "sonner";

import { deleteFlashcardSet } from "@/api/flashcardSetService";
import { getUserFlashcardSetsByUserId } from "@/api/userService";
import FolderListComponent from "@/app/profile/folders/components/folder-list-component";
import EmptySetsState from "@/components/sets/empty-sets-state";
import StudySetCard, {
  type StudySetListItem,
} from "@/components/sets/study-set-card";
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
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useAuth } from "@/context/AuthContext";
import { parseJwt } from "@/utils/jwt";

interface DecodedToken {
  id?: number | string;
}

interface FlashcardSetDTO {
  setId: number;
  user: {
    nickname: string;
  };
  setName: string;
  description: string;
  termCount: number;
}

type StudySet = StudySetListItem;

export default function MyProfileSetsPage() {
  const { token, isAuthLoading } = useAuth();

  const [sets, setSets] = useState<StudySet[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const [setToDelete, setSetToDelete] = useState<StudySet | null>(null);

  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);

  const [isDeleting, setIsDeleting] = useState(false);

  const [setToAddToFolder, setSetToAddToFolder] = useState<StudySet | null>(
    null,
  );

  const [isFolderListOpen, setIsFolderListOpen] = useState(false);

  useEffect(() => {
    if (isAuthLoading) {
      return;
    }

    let isCancelled = false;

    async function fetchSets() {
      if (!token) {
        setSets([]);
        setIsLoading(false);
        return;
      }

      const decoded = parseJwt(token) as DecodedToken | null;
      const userId = Number(decoded?.id);

      if (
        decoded?.id === undefined ||
        decoded.id === null ||
        Number.isNaN(userId)
      ) {
        setSets([]);
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);

        const setsData = await getUserFlashcardSetsByUserId(userId, token);

        if (isCancelled) {
          return;
        }

        const formattedSets: StudySet[] = setsData.map(
          (set: FlashcardSetDTO) => ({
            id: set.setId.toString(),
            title: set.setName,
            description: set.description || "",
            termCount: Number(set.termCount),
            nickname: set.user.nickname,
          }),
        );

        setSets(formattedSets);
      } catch (error: unknown) {
        if (isCancelled) {
          return;
        }

        console.error("Error fetching sets:", error);

        toast.error("Failed to load study sets.");

        setSets([]);
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    }

    void fetchSets();

    return () => {
      isCancelled = true;
    };
  }, [token, isAuthLoading]);

  const normalizedSearchQuery = searchQuery.trim().toLowerCase();

  const filteredSets = useMemo(() => {
    if (!normalizedSearchQuery) {
      return sets;
    }

    return sets.filter(
      (set) =>
        set.title.toLowerCase().includes(normalizedSearchQuery) ||
        set.description.toLowerCase().includes(normalizedSearchQuery),
    );
  }, [sets, normalizedSearchQuery]);

  function handleDeleteClick(set: StudySet) {
    setSetToDelete(set);
    setIsDeleteDialogOpen(true);
  }

  function handleDeleteCancel() {
    if (isDeleting) {
      return;
    }

    setSetToDelete(null);
    setIsDeleteDialogOpen(false);
  }

  function handleDeleteDialogOpenChange(open: boolean) {
    if (isDeleting) {
      return;
    }

    setIsDeleteDialogOpen(open);

    if (!open) {
      setSetToDelete(null);
    }
  }

  async function handleDeleteConfirm() {
    if (!setToDelete || !token) {
      return;
    }

    try {
      setIsDeleting(true);

      await deleteFlashcardSet(setToDelete.id, token);

      setSets((currentSets) =>
        currentSets.filter((set) => set.id !== setToDelete.id),
      );

      toast.success("Set deleted successfully.");

      setIsDeleteDialogOpen(false);
      setSetToDelete(null);
    } catch (error: unknown) {
      console.error("Failed to delete set:", error);

      const message =
        error instanceof Error
          ? error.message
          : "Failed to delete set. Please try again.";

      toast.error(message);
    } finally {
      setIsDeleting(false);
    }
  }

  function handleDeleteActionClick(event: MouseEvent<HTMLButtonElement>) {
    event.preventDefault();
    void handleDeleteConfirm();
  }

  function handleAddToFolderClick(set: StudySet) {
    setSetToAddToFolder(set);
    setIsFolderListOpen(true);
  }

  function handleFolderListClose() {
    setSetToAddToFolder(null);
    setIsFolderListOpen(false);
  }

  function handleFolderUpdated() {
    setSetToAddToFolder(null);
    setIsFolderListOpen(false);
  }

  const isPageLoading = isAuthLoading || isLoading;

  return (
    <>
      <div className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
        <div className="container mx-auto px-4 py-8">
          <div className="mb-8 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div>
              <h1 className="text-3xl font-bold text-foreground">Your sets</h1>

              <p className="mt-2 text-muted-foreground">
                Manage your flashcard sets and add them to folders.
              </p>
            </div>

            <Button
              asChild
              className="bg-pink-500 text-white hover:bg-pink-600"
            >
              <Link href="/create-set">
                <Plus className="mr-2 h-4 w-4" aria-hidden="true" />
                Create set
              </Link>
            </Button>
          </div>

          <div className="relative mb-6">
            <Search
              className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
              aria-hidden="true"
            />

            <Input
              type="search"
              value={searchQuery}
              onChange={(event) => setSearchQuery(event.target.value)}
              placeholder="Search your sets..."
              className="border-input bg-background pl-10 text-foreground placeholder:text-muted-foreground focus-visible:border-pink-300 focus-visible:ring-pink-500/20 dark:focus-visible:border-pink-800"
              aria-label="Search your sets"
            />
          </div>

          {isPageLoading ? (
            <div
              className="flex items-center justify-center gap-3 py-10 text-muted-foreground"
              role="status"
            >
              <Loader2
                className="h-5 w-5 animate-spin text-pink-500 dark:text-pink-400"
                aria-hidden="true"
              />

              <span>Downloading study sets...</span>
            </div>
          ) : !token ? (
            <EmptySetsState
              title="You are not logged in"
              description="Log in to see and manage your flashcard sets."
            />
          ) : filteredSets.length > 0 ? (
            <div className="grid gap-4 md:grid-cols-2">
              {filteredSets.map((set) => (
                <StudySetCard
                  key={set.id}
                  set={set}
                  showOwnerActions
                  onDeleteClick={handleDeleteClick}
                  onAddToFolderClick={handleAddToFolderClick}
                  isMenuForcedOpen={
                    (isDeleteDialogOpen && setToDelete?.id === set.id) ||
                    (isFolderListOpen && setToAddToFolder?.id === set.id)
                  }
                />
              ))}
            </div>
          ) : (
            <EmptySetsState
              title={
                normalizedSearchQuery
                  ? "No sets found"
                  : "You don't have any sets yet"
              }
              description={
                normalizedSearchQuery
                  ? "Try using a different search phrase."
                  : "Create your first flashcard set to start learning."
              }
              showCreateButton
            />
          )}
        </div>
      </div>

      <AlertDialog
        open={isDeleteDialogOpen}
        onOpenChange={handleDeleteDialogOpenChange}
      >
        <AlertDialogContent className="border-border bg-background text-foreground">
          <AlertDialogHeader>
            <AlertDialogTitle className="text-foreground">
              Delete set?
            </AlertDialogTitle>

            <AlertDialogDescription className="text-muted-foreground">
              This action cannot be undone. This will permanently delete the set
              &quot;{setToDelete?.title}&quot; and all flashcards inside it.
            </AlertDialogDescription>
          </AlertDialogHeader>

          <AlertDialogFooter>
            <AlertDialogCancel
              disabled={isDeleting}
              onClick={handleDeleteCancel}
              className="border-border bg-background text-foreground hover:bg-accent hover:text-accent-foreground"
            >
              Cancel
            </AlertDialogCancel>

            <AlertDialogAction
              onClick={handleDeleteActionClick}
              disabled={isDeleting}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90 disabled:bg-muted disabled:text-muted-foreground"
            >
              {isDeleting ? "Deleting..." : "Delete"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {setToAddToFolder && (
        <FolderListComponent
          flashcardSetId={setToAddToFolder.id}
          flashcardSetTitle={setToAddToFolder.title}
          isOpen={isFolderListOpen}
          onClose={handleFolderListClose}
          onFolderUpdated={handleFolderUpdated}
        />
      )}
    </>
  );
}
