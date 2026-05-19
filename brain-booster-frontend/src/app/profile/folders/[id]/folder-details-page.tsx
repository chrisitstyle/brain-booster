"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import {
  ArrowLeft,
  BookOpen,
  FolderOpen,
  MoreHorizontal,
  Plus,
  User,
} from "lucide-react";
import { toast } from "sonner";

import { useAuth } from "@/context/AuthContext";
import {
  getFolderDetailsById,
  removeSetFromFolder,
  type FlashcardSetInFolder,
  type Folder,
} from "@/api/folderService";

import SetListComponent from "./set-list-component";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
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

interface FolderDetailsPageProps {
  folderId: string;
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
    const fetchFolderDetails = async () => {
      if (!folderId) return;

      try {
        setIsLoading(true);
        const data = await getFolderDetailsById(folderId);
        setFolder(data);
      } catch (error) {
        console.error("Error fetching folder details:", error);

        toast.error("Failed to load folder details.", {
          style: {
            background: "red",
            color: "white",
          },
        });

        setFolder(null);
      } finally {
        setIsLoading(false);
      }
    };

    fetchFolderDetails();
  }, [folderId]);

  const handleFolderUpdated = (updatedFolder: Folder) => {
    setFolder(updatedFolder);
  };

  const handleRemoveClick = (set: FlashcardSetInFolder) => {
    setSetToRemove(set);
    setIsRemoveDialogOpen(true);
  };

  const handleRemoveCancel = () => {
    if (isRemoving) return;

    setSetToRemove(null);
    setIsRemoveDialogOpen(false);
  };

  const handleRemoveConfirm = async () => {
    if (!setToRemove || !folder || !token) {
      toast.error("You must be logged in to remove sets from folder.", {
        style: {
          background: "red",
          color: "white",
        },
      });
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
        if (!currentFolder) return currentFolder;

        return {
          ...currentFolder,
          setCount: Math.max(Number(currentFolder.setCount) - 1, 0),
          flashcardSets: currentFolder.flashcardSets.filter(
            (set) => set.flashcardSetId !== setToRemove.flashcardSetId,
          ),
        };
      });

      toast.success("Set removed from folder", {
        style: {
          background: "green",
          color: "white",
        },
      });
    } catch (error) {
      console.error("Failed to remove set from folder:", error);

      toast.error("Failed to remove set from folder. Please try again.", {
        style: {
          background: "red",
          color: "white",
        },
      });
    } finally {
      setIsRemoving(false);
      setIsRemoveDialogOpen(false);
      setSetToRemove(null);
    }
  };

  return (
    <>
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto max-w-5xl px-4 py-8">
          <div className="mb-6">
            <Button
              variant="ghost"
              size="sm"
              className="text-gray-500 hover:text-pink-500"
              asChild
            >
              <Link href="/profile/folders">
                <ArrowLeft className="mr-2 h-4 w-4" />
                Back to folders
              </Link>
            </Button>
          </div>

          {isLoading ? (
            <div className="py-16 text-center text-gray-500">
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
                  <h2 className="text-lg font-semibold text-gray-800">
                    Sets in this folder ({folder.setCount})
                  </h2>

                  <Button
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
                          setToRemove?.flashcardSetId === set.flashcardSetId
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
      </div>

      <AlertDialog
        open={isRemoveDialogOpen}
        onOpenChange={setIsRemoveDialogOpen}
      >
        <AlertDialogContent>
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
              onClick={handleRemoveConfirm}
              className="bg-red-500 text-white hover:bg-red-600"
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
    <Card className="border-gray-200 bg-white shadow-sm">
      <CardContent className="p-6">
        <div className="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
          <div className="flex gap-4">
            <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-pink-100">
              <FolderOpen className="h-7 w-7 text-pink-500" />
            </div>

            <div className="min-w-0">
              <h1 className="text-2xl font-bold text-gray-800">
                {folder.name}
              </h1>

              {folder.description && (
                <p className="mt-2 max-w-2xl text-sm leading-6 text-gray-600">
                  {folder.description}
                </p>
              )}

              <div className="mt-4 flex flex-wrap items-center gap-3 text-sm text-gray-500">
                <span className="font-medium">
                  {folder.setCount} {folder.setCount === 1 ? "set" : "sets"}
                </span>

                <span className="h-1 w-1 rounded-full bg-gray-300" />

                <Link
                  href={`/users/${folder.nickname}/profile`}
                  className="flex items-center gap-2 hover:text-pink-500"
                >
                  <Avatar className="h-6 w-6">
                    <AvatarFallback className="bg-pink-100 text-xs text-pink-500">
                      <User className="h-3 w-3" />
                    </AvatarFallback>
                  </Avatar>
                  {folder.nickname}
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
}: {
  set: FlashcardSetInFolder;
  nickname: string;
  onRemoveClick: (set: FlashcardSetInFolder) => void;
  isRemoveDialogOpen: boolean;
}) {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  return (
    <Card className="group border-gray-200 bg-white transition-all hover:border-pink-200 hover:shadow-md">
      <CardContent className="p-5">
        <div className="flex items-start gap-4">
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-pink-50">
            <BookOpen className="h-5 w-5 text-pink-500" />
          </div>

          <div className="min-w-0 flex-1">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <Link
                  href={`/users/${nickname}/sets/${set.flashcardSetId}`}
                  className="line-clamp-1 text-base font-semibold text-gray-800 hover:text-pink-500"
                >
                  {set.title}
                </Link>

                <p className="mt-1 text-sm text-gray-500">
                  {set.termCount} {set.termCount === 1 ? "term" : "terms"}
                </p>
              </div>

              <DropdownMenu
                open={isDropdownOpen}
                onOpenChange={setIsDropdownOpen}
              >
                <DropdownMenuTrigger asChild>
                  <Button
                    variant="ghost"
                    size="icon"
                    className={
                      isDropdownOpen || isRemoveDialogOpen
                        ? "h-8 w-8 opacity-100"
                        : "h-8 w-8 opacity-0 transition-opacity group-hover:opacity-100"
                    }
                  >
                    <MoreHorizontal className="h-4 w-4 text-gray-400" />
                  </Button>
                </DropdownMenuTrigger>

                <DropdownMenuContent align="end">
                  <DropdownMenuItem
                    className="cursor-pointer text-red-500"
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

function EmptyState({
  title,
  description,
}: {
  title: string;
  description: string;
}) {
  return (
    <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-gray-200 bg-white py-16 text-center">
      <div className="mb-4 rounded-full bg-gray-100 p-4">
        <FolderOpen className="h-8 w-8 text-gray-400" />
      </div>

      <h2 className="text-lg font-semibold text-gray-800">{title}</h2>
      <p className="mt-1 max-w-sm text-sm text-gray-500">{description}</p>
    </div>
  );
}
