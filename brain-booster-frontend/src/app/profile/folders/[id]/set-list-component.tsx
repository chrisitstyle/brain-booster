"use client";

import { useEffect, useState } from "react";
import { BookOpen, Search, X } from "lucide-react";
import { toast } from "sonner";

import {
  addSetToFolder,
  type FlashcardSetInFolder,
  type Folder,
} from "@/api/folderService";
import { getUserFlashcardSetsByUserId } from "@/api/userService";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useAuth } from "@/context/AuthContext";
import { parseJwt } from "@/utils/jwt";

interface FlashcardSetDTO {
  setId: number;
  setName: string;
  description: string;
  termCount: number;
}

interface SetListComponentProps {
  folderId: number | string;
  folderName: string;
  currentSets: FlashcardSetInFolder[];
  isOpen: boolean;
  onClose: () => void;
  onFolderUpdated: (folder: Folder) => void;
}

export default function SetListComponent({
  folderId,
  folderName,
  currentSets,
  isOpen,
  onClose,
  onFolderUpdated,
}: SetListComponentProps) {
  const { token, isAuthLoading } = useAuth();

  const [sets, setSets] = useState<FlashcardSetDTO[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [addingSetId, setAddingSetId] = useState<number | null>(null);

  useEffect(() => {
    if (!isOpen || isAuthLoading) {
      return;
    }

    let isCancelled = false;

    async function fetchUserSets() {
      if (!token) {
        setSets([]);
        return;
      }

      const decodedToken = parseJwt(token);

      if (!decodedToken?.id) {
        setSets([]);
        return;
      }

      try {
        setIsLoading(true);

        const data = await getUserFlashcardSetsByUserId(decodedToken.id, token);

        if (!isCancelled) {
          setSets(data);
        }
      } catch (error: unknown) {
        if (isCancelled) {
          return;
        }

        console.error("Failed to fetch user sets:", error);

        toast.error("Failed to load your sets.");
        setSets([]);
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    }

    void fetchUserSets();

    return () => {
      isCancelled = true;
    };
  }, [isOpen, token, isAuthLoading]);

  if (!isOpen) {
    return null;
  }

  const currentSetIds = new Set(
    currentSets.map((set) => Number(set.flashcardSetId)),
  );

  const setsNotInFolder = sets.filter(
    (set) => !currentSetIds.has(Number(set.setId)),
  );

  const normalizedSearchQuery = searchQuery.trim().toLowerCase();

  const filteredSets = setsNotInFolder.filter((set) =>
    set.setName.toLowerCase().includes(normalizedSearchQuery),
  );

  async function handleAddSet(set: FlashcardSetDTO) {
    if (!token) {
      toast.error("You must be logged in to add sets to folder.");
      return;
    }

    try {
      setAddingSetId(set.setId);

      const updatedFolder = await addSetToFolder(
        {
          folderId,
          flashcardSetId: set.setId,
        },
        token,
      );

      onFolderUpdated(updatedFolder);

      toast.success("Set added to folder");
    } catch (error: unknown) {
      console.error("Failed to add set to folder:", error);

      const message =
        error instanceof Error ? error.message : "Failed to add set to folder.";

      toast.error(message);
    } finally {
      setAddingSetId(null);
    }
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="set-list-title"
      aria-describedby="set-list-description"
    >
      <Card className="max-h-[85vh] w-full max-w-2xl overflow-hidden border-border bg-card text-card-foreground shadow-xl">
        <div className="flex items-start justify-between gap-4 border-b border-border p-5">
          <div className="min-w-0">
            <h2
              id="set-list-title"
              className="text-xl font-bold text-card-foreground"
            >
              Add set to folder
            </h2>

            <p
              id="set-list-description"
              className="mt-1 truncate text-sm text-muted-foreground"
            >
              {folderName}
            </p>
          </div>

          <Button
            type="button"
            variant="ghost"
            size="icon"
            onClick={onClose}
            disabled={addingSetId !== null}
            className="shrink-0 text-muted-foreground hover:bg-accent hover:text-foreground"
            aria-label="Close set list"
          >
            <X className="h-5 w-5" aria-hidden="true" />
          </Button>
        </div>

        <CardContent className="p-5">
          <div className="mb-5">
            <div className="relative">
              <Search
                className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
                aria-hidden="true"
              />

              <Input
                type="search"
                placeholder="Search your sets..."
                value={searchQuery}
                onChange={(event) => setSearchQuery(event.target.value)}
                autoFocus
                className="border-input bg-background pl-10 text-foreground placeholder:text-muted-foreground focus-visible:border-pink-300 focus-visible:ring-pink-500/20 dark:focus-visible:border-pink-800"
                aria-label="Search your sets"
              />
            </div>
          </div>

          {isLoading || isAuthLoading ? (
            <div className="py-10 text-center text-muted-foreground">
              Downloading sets...
            </div>
          ) : !token ? (
            <div className="py-10 text-center text-muted-foreground">
              You must be logged in to add sets.
            </div>
          ) : filteredSets.length > 0 ? (
            <div className="max-h-[50vh] space-y-3 overflow-y-auto pr-1">
              {filteredSets.map((set) => {
                const isAdding = addingSetId === set.setId;
                const isAnotherSetAdding = addingSetId !== null && !isAdding;

                return (
                  <div
                    key={set.setId}
                    className="flex items-center justify-between gap-4 rounded-lg border border-border bg-background p-4 transition-colors hover:border-pink-200 hover:bg-pink-50/50 dark:hover:border-pink-900 dark:hover:bg-pink-950/20"
                  >
                    <div className="flex min-w-0 items-start gap-3">
                      <div className="shrink-0 rounded-lg bg-pink-100 p-2 dark:bg-pink-950/50">
                        <BookOpen className="h-5 w-5 text-pink-500 dark:text-pink-400" />
                      </div>

                      <div className="min-w-0">
                        <h3 className="line-clamp-1 font-semibold text-foreground">
                          {set.setName}
                        </h3>

                        {set.description && (
                          <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
                            {set.description}
                          </p>
                        )}

                        <p className="mt-1 text-xs font-medium uppercase tracking-wide text-muted-foreground">
                          {set.termCount}{" "}
                          {set.termCount === 1 ? "term" : "terms"}
                        </p>
                      </div>
                    </div>

                    <Button
                      type="button"
                      size="sm"
                      disabled={isAdding || isAnotherSetAdding}
                      onClick={() => {
                        void handleAddSet(set);
                      }}
                      className="shrink-0 bg-pink-500 text-white hover:bg-pink-600 disabled:bg-muted disabled:text-muted-foreground"
                    >
                      {isAdding ? "Adding..." : "Add"}
                    </Button>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="py-10 text-center text-muted-foreground">
              {normalizedSearchQuery
                ? "No sets found for this search."
                : sets.length === 0
                  ? "You do not have any sets yet."
                  : "All your sets are already in this folder."}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
