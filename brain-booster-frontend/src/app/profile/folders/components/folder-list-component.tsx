"use client";

import { useEffect, useState } from "react";
import { FolderOpen, Loader2, Search, X } from "lucide-react";
import { toast } from "sonner";

import { addSetToFolder, getMyFolders, type Folder } from "@/api/folderService";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useAuth } from "@/context/AuthContext";

interface FolderListComponentProps {
  flashcardSetId: number | string;
  flashcardSetTitle: string;
  isOpen: boolean;
  onClose: () => void;
  onFolderUpdated?: (folder: Folder) => void;
}

export default function FolderListComponent({
  flashcardSetId,
  flashcardSetTitle,
  isOpen,
  onClose,
  onFolderUpdated,
}: FolderListComponentProps) {
  const { token, isAuthLoading } = useAuth();

  const [folders, setFolders] = useState<Folder[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [addingFolderId, setAddingFolderId] = useState<number | null>(null);

  useEffect(() => {
    if (!isOpen || isAuthLoading || !token) {
      return;
    }

    let isCancelled = false;
    const requestToken = token;

    async function fetchFolders() {
      try {
        setIsLoading(true);

        const data = await getMyFolders(requestToken);

        if (isCancelled) {
          return;
        }

        setFolders(data);
      } catch (error: unknown) {
        if (isCancelled) {
          return;
        }

        console.error("Failed to fetch folders:", error);

        toast.error("Failed to load folders.");
        setFolders([]);
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    }

    void fetchFolders();

    return () => {
      isCancelled = true;
    };
  }, [isOpen, token, isAuthLoading]);

  if (!isOpen) {
    return null;
  }

  const normalizedSearchQuery = searchQuery.trim().toLowerCase();

  const visibleFolders = token ? folders : [];

  const filteredFolders = visibleFolders.filter((folder) =>
    folder.name.toLowerCase().includes(normalizedSearchQuery),
  );

  async function handleAddToFolder(folder: Folder) {
    if (!token) {
      toast.error("You must be logged in to add sets to folders.");

      return;
    }

    try {
      setAddingFolderId(folder.folderId);

      const updatedFolder = await addSetToFolder(
        {
          folderId: folder.folderId,
          flashcardSetId,
        },
        token,
      );

      setFolders((previousFolders) =>
        previousFolders.map((currentFolder) =>
          currentFolder.folderId === updatedFolder.folderId
            ? updatedFolder
            : currentFolder,
        ),
      );

      onFolderUpdated?.(updatedFolder);

      toast.success("Set added to folder");
    } catch (error: unknown) {
      console.error("Failed to add set to folder:", error);

      const message =
        error instanceof Error ? error.message : "Failed to add set to folder.";

      toast.error(message);
    } finally {
      setAddingFolderId(null);
    }
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="folder-list-title"
      aria-describedby="folder-list-description"
    >
      <Card className="max-h-[85vh] w-full max-w-2xl overflow-hidden border-border bg-card text-card-foreground shadow-xl">
        <div className="flex items-start justify-between gap-4 border-b border-border p-5">
          <div className="min-w-0">
            <h2
              id="folder-list-title"
              className="text-xl font-bold text-card-foreground"
            >
              Add to folder
            </h2>

            <p
              id="folder-list-description"
              className="mt-1 truncate text-sm text-muted-foreground"
            >
              {flashcardSetTitle}
            </p>
          </div>

          <Button
            type="button"
            variant="ghost"
            size="icon"
            onClick={onClose}
            className="shrink-0 text-muted-foreground hover:bg-accent hover:text-foreground"
            aria-label="Close folder list"
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
                placeholder="Search folders..."
                value={searchQuery}
                onChange={(event) => setSearchQuery(event.target.value)}
                autoFocus
                className="border-input bg-background pl-10 text-foreground placeholder:text-muted-foreground focus-visible:border-pink-300 focus-visible:ring-pink-500/20 dark:focus-visible:border-pink-800"
                aria-label="Search folders"
              />
            </div>
          </div>

          {isAuthLoading ? (
            <LoadingState text="Checking authentication..." />
          ) : !token ? (
            <div className="py-10 text-center text-muted-foreground">
              You must be logged in to add sets to folders.
            </div>
          ) : isLoading ? (
            <LoadingState text="Downloading folders..." />
          ) : filteredFolders.length > 0 ? (
            <div className="max-h-[50vh] space-y-3 overflow-y-auto pr-1">
              {filteredFolders.map((folder) => {
                const isAlreadyInFolder = folder.flashcardSets.some(
                  (set) =>
                    Number(set.flashcardSetId) === Number(flashcardSetId),
                );

                const isAdding = addingFolderId === folder.folderId;

                return (
                  <div
                    key={folder.folderId}
                    className="flex items-center justify-between gap-4 rounded-lg border border-border bg-background p-4 transition-colors hover:border-pink-200 hover:bg-pink-50/50 dark:hover:border-pink-900 dark:hover:bg-pink-950/20"
                  >
                    <div className="flex min-w-0 items-start gap-3">
                      <div className="shrink-0 rounded-lg bg-pink-100 p-2 dark:bg-pink-950/50">
                        <FolderOpen
                          className="h-5 w-5 text-pink-500 dark:text-pink-400"
                          aria-hidden="true"
                        />
                      </div>

                      <div className="min-w-0">
                        <h3 className="line-clamp-1 font-semibold text-foreground">
                          {folder.name}
                        </h3>

                        {folder.description && (
                          <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
                            {folder.description}
                          </p>
                        )}

                        <p className="mt-1 text-xs font-medium uppercase tracking-wide text-muted-foreground">
                          {folder.setCount}{" "}
                          {folder.setCount === 1 ? "set" : "sets"}
                        </p>
                      </div>
                    </div>

                    <Button
                      type="button"
                      size="sm"
                      disabled={isAlreadyInFolder || isAdding}
                      onClick={() => {
                        void handleAddToFolder(folder);
                      }}
                      className="shrink-0 bg-pink-500 text-white hover:bg-pink-600 disabled:bg-muted disabled:text-muted-foreground"
                    >
                      {isAlreadyInFolder
                        ? "Added"
                        : isAdding
                          ? "Adding..."
                          : "Add"}
                    </Button>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="py-10 text-center text-muted-foreground">
              {searchQuery.trim()
                ? "No folders found for this search."
                : "You do not have any folders yet."}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

function LoadingState({ text }: { text: string }) {
  return (
    <div
      className="flex items-center justify-center gap-3 py-10 text-muted-foreground"
      role="status"
    >
      <Loader2
        className="h-5 w-5 animate-spin text-pink-500 dark:text-pink-400"
        aria-hidden="true"
      />

      <span>{text}</span>
    </div>
  );
}
