"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import {
  ArrowLeft,
  BookOpen,
  FolderOpen,
  MoreHorizontal,
  Plus,
  User,
} from "lucide-react";
import { toast } from "sonner";

import {
  getFolderDetailsById,
  removeSetFromFolder,
  type FlashcardSetInFolder,
  type Folder,
} from "@/api/folderService";
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
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useAuth } from "@/context/AuthContext";
import { cn } from "@/lib/utils";

import SetListComponent from "./set-list-component";

interface FolderDetailsPageProps {
  folderId: string;
}

interface FlashcardSetCardProps {
  set: FlashcardSetInFolder;
  nickname: string;
  onRemoveClick: (set: FlashcardSetInFolder) => void;
  isRemoveDialogOpen: boolean;
}

interface EmptyStateProps {
  title: string;
  description: string;
}

export default function FolderDetailsPage({
  folderId,
}: FolderDetailsPageProps) {
  const { token } = useAuth();

  const [folder, setFolder] = useState<Folder | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const [isSetListOpen, setIsSetListOpen] = useState(false);

  const [setToRemove, setSetToRemove] = useState<FlashcardSetInFolder | null>(
    null,
  );

  const [isRemoveDialogOpen, setIsRemoveDialogOpen] = useState(false);

  const [isRemoving, setIsRemoving] = useState(false);

  useEffect(() => {
    if (!folderId) {
      return;
    }

    let isCancelled = false;

    async function fetchFolderDetails() {
      try {
        setIsLoading(true);

        const data = await getFolderDetailsById(folderId);

        if (!isCancelled) {
          setFolder(data);
        }
      } catch (error: unknown) {
        if (isCancelled) {
          return;
        }

        console.error("Error fetching folder details:", error);

        toast.error("Failed to load folder details.");
        setFolder(null);
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    }

    void fetchFolderDetails();

    return () => {
      isCancelled = true;
    };
  }, [folderId]);

  function handleFolderUpdated(updatedFolder: Folder) {
    setFolder(updatedFolder);
  }

  function handleRemoveClick(set: FlashcardSetInFolder) {
    setSetToRemove(set);
    setIsRemoveDialogOpen(true);
  }

  function handleRemoveCancel() {
    if (isRemoving) {
      return;
    }

    setSetToRemove(null);
    setIsRemoveDialogOpen(false);
  }

  async function handleRemoveConfirm() {
    if (!setToRemove || !folder || !token) {
      toast.error("You must be logged in to remove sets from folder.");

      return;
    }

    try {
      setIsRemoving(true);

      await removeSetFromFolder(
        folder.folderId,
        setToRemove.flashcardSetId,
        token,
      );

      setFolder((currentFolder) => {
        if (!currentFolder) {
          return currentFolder;
        }

        return {
          ...currentFolder,
          setCount: Math.max(Number(currentFolder.setCount) - 1, 0),
          flashcardSets: currentFolder.flashcardSets.filter(
            (set) =>
              Number(set.flashcardSetId) !== Number(setToRemove.flashcardSetId),
          ),
        };
      });

      toast.success("Set removed from folder");
    } catch (error: unknown) {
      console.error("Failed to remove set from folder:", error);

      toast.error("Failed to remove set from folder. Please try again.");
    } finally {
      setIsRemoving(false);
      setIsRemoveDialogOpen(false);
      setSetToRemove(null);
    }
  }

  return (
    <>
      <div className="container mx-auto max-w-5xl px-4 py-8">
        <div className="mb-6">
          <Button
            variant="ghost"
            size="sm"
            className="text-muted-foreground hover:bg-accent hover:text-pink-500 dark:hover:text-pink-400"
            asChild
          >
            <Link href="/profile/folders">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to folders
            </Link>
          </Button>
        </div>

        {isLoading ? (
          <div className="py-16 text-center text-muted-foreground">
            Downloading folder details...
          </div>
        ) : !folder ? (
          <EmptyState
            title="Folder not found"
            description="This folder does not exist or could not be loaded."
          />
        ) : (
          <>
            <FolderHeader folder={folder} />

            <div className="mt-8">
              <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <h2 className="text-lg font-semibold text-foreground">
                  Sets in this folder ({folder.setCount})
                </h2>

                <Button
                  type="button"
                  className="bg-pink-500 text-white hover:bg-pink-600"
                  onClick={() => setIsSetListOpen(true)}
                >
                  <Plus className="mr-2 h-4 w-4" />
                  Add Set
                </Button>
              </div>

              {folder.flashcardSets.length > 0 ? (
                <div className="space-y-4">
                  {folder.flashcardSets.map((set) => (
                    <FlashcardSetCard
                      key={set.flashcardSetId}
                      set={set}
                      nickname={folder.nickname}
                      onRemoveClick={handleRemoveClick}
                      isRemoveDialogOpen={
                        isRemoveDialogOpen &&
                        Number(setToRemove?.flashcardSetId) ===
                          Number(set.flashcardSetId)
                      }
                    />
                  ))}
                </div>
              ) : (
                <EmptyState
                  title="No sets in this folder"
                  description="This folder does not contain any flashcard sets yet."
                />
              )}
            </div>

            <SetListComponent
              folderId={folder.folderId}
              folderName={folder.name}
              currentSets={folder.flashcardSets}
              isOpen={isSetListOpen}
              onClose={() => setIsSetListOpen(false)}
              onFolderUpdated={handleFolderUpdated}
            />
          </>
        )}
      </div>

      <AlertDialog
        open={isRemoveDialogOpen}
        onOpenChange={(open) => {
          if (isRemoving) {
            return;
          }

          setIsRemoveDialogOpen(open);

          if (!open) {
            setSetToRemove(null);
          }
        }}
      >
        <AlertDialogContent className="border-border bg-background text-foreground">
          <AlertDialogHeader>
            <AlertDialogTitle>Remove set from folder?</AlertDialogTitle>

            <AlertDialogDescription>
              This will remove &quot;{setToRemove?.title}&quot; from this
              folder. The flashcard set itself will not be deleted.
            </AlertDialogDescription>
          </AlertDialogHeader>

          <AlertDialogFooter>
            <AlertDialogCancel
              disabled={isRemoving}
              onClick={handleRemoveCancel}
            >
              Cancel
            </AlertDialogCancel>

            <AlertDialogAction
              disabled={isRemoving}
              onClick={() => {
                void handleRemoveConfirm();
              }}
              className="bg-red-500 text-white hover:bg-red-600 dark:bg-red-600 dark:hover:bg-red-700"
            >
              {isRemoving ? "Removing..." : "Remove"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}

function FolderHeader({ folder }: { folder: Folder }) {
  return (
    <Card className="border-border bg-card text-card-foreground shadow-sm">
      <CardContent className="p-6">
        <div className="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
          <div className="flex min-w-0 gap-4">
            <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-pink-100 dark:bg-pink-950/50">
              <FolderOpen className="h-7 w-7 text-pink-500 dark:text-pink-400" />
            </div>

            <div className="min-w-0">
              <h1 className="break-words text-2xl font-bold text-card-foreground">
                {folder.name}
              </h1>

              {folder.description && (
                <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
                  {folder.description}
                </p>
              )}

              <div className="mt-4 flex flex-wrap items-center gap-3 text-sm text-muted-foreground">
                <span className="font-medium">
                  {folder.setCount} {folder.setCount === 1 ? "set" : "sets"}
                </span>

                <span className="h-1 w-1 rounded-full bg-muted-foreground/50" />

                <Link
                  href={`/users/${encodeURIComponent(folder.nickname)}/profile`}
                  className="flex items-center gap-2 transition-colors hover:text-pink-500 dark:hover:text-pink-400"
                >
                  <Avatar className="h-6 w-6">
                    <AvatarFallback className="bg-pink-100 text-xs text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
                      <User className="h-3 w-3" aria-hidden="true" />
                    </AvatarFallback>
                  </Avatar>

                  <span className="truncate">{folder.nickname}</span>
                </Link>
              </div>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

function FlashcardSetCard({
  set,
  nickname,
  onRemoveClick,
  isRemoveDialogOpen,
}: FlashcardSetCardProps) {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  return (
    <Card className="group border-border bg-card text-card-foreground transition-all hover:border-pink-200 hover:shadow-md dark:hover:border-pink-900">
      <CardContent className="p-5">
        <div className="flex items-start gap-4">
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-pink-100 dark:bg-pink-950/50">
            <BookOpen className="h-5 w-5 text-pink-500 dark:text-pink-400" />
          </div>

          <div className="min-w-0 flex-1">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <Link
                  href={`/users/${encodeURIComponent(nickname)}/sets/${set.flashcardSetId}`}
                  className="line-clamp-1 text-base font-semibold text-card-foreground transition-colors hover:text-pink-500 dark:hover:text-pink-400"
                >
                  {set.title}
                </Link>

                <p className="mt-1 text-sm text-muted-foreground">
                  {set.termCount} {set.termCount === 1 ? "term" : "terms"}
                </p>
              </div>

              <DropdownMenu
                open={isDropdownOpen}
                onOpenChange={setIsDropdownOpen}
              >
                <DropdownMenuTrigger asChild>
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    className={cn(
                      "h-8 w-8 shrink-0 transition-opacity",
                      isDropdownOpen || isRemoveDialogOpen
                        ? "opacity-100"
                        : "opacity-0 group-hover:opacity-100 group-focus-within:opacity-100",
                    )}
                    aria-label={`Open options for ${set.title}`}
                  >
                    <MoreHorizontal className="h-4 w-4 text-muted-foreground" />
                  </Button>
                </DropdownMenuTrigger>

                <DropdownMenuContent
                  align="end"
                  className="border-border bg-popover text-popover-foreground"
                >
                  <DropdownMenuItem
                    className="cursor-pointer text-red-500 focus:bg-red-50 focus:text-red-600 dark:focus:bg-red-950/40 dark:focus:text-red-400"
                    onSelect={(event) => {
                      event.preventDefault();
                      onRemoveClick(set);
                      setIsDropdownOpen(false);
                    }}
                  >
                    Remove from folder
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

function EmptyState({ title, description }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-border bg-card px-4 py-16 text-center text-card-foreground">
      <div className="mb-4 rounded-full bg-muted p-4">
        <FolderOpen className="h-8 w-8 text-muted-foreground" />
      </div>

      <h2 className="text-lg font-semibold text-card-foreground">{title}</h2>

      <p className="mt-1 max-w-sm text-sm text-muted-foreground">
        {description}
      </p>
    </div>
  );
}
