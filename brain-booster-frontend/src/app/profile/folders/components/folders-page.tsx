"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { FolderOpen, MoreHorizontal, Plus, Search, User } from "lucide-react";
import { toast } from "sonner";

import {
  deleteFolderById,
  getMyFolders,
  type Folder as FolderDTO,
} from "@/api/folderService";
import EditFolderForm from "@/app/profile/folders/components/edit-folder-form";
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
import { Input } from "@/components/ui/input";
import { useAuth } from "@/context/AuthContext";
import { cn } from "@/lib/utils";

interface DashboardFolder {
  id: string;
  title: string;
  description: string;
  nickname: string;
  setCount: number;
}

export default function FoldersPage() {
  const { token, isAuthLoading } = useAuth();

  const [folders, setFolders] = useState<DashboardFolder[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);

  const [folderToDelete, setFolderToDelete] = useState<DashboardFolder | null>(
    null,
  );

  const [isDeleting, setIsDeleting] = useState(false);

  const [isEditFormOpen, setIsEditFormOpen] = useState(false);

  const [folderToEdit, setFolderToEdit] = useState<DashboardFolder | null>(
    null,
  );

  useEffect(() => {
    if (isAuthLoading || !token) {
      return;
    }

    let isCancelled = false;
    const requestToken = token;

    async function fetchFolders() {
      try {
        setIsLoading(true);

        const foldersData = await getMyFolders(requestToken);

        if (isCancelled) {
          return;
        }

        const formattedFolders: DashboardFolder[] = foldersData.map(
          (folder: FolderDTO) => ({
            id: folder.folderId.toString(),
            title: folder.name,
            description: folder.description ?? "",
            nickname: folder.nickname,
            setCount: Number(folder.setCount),
          }),
        );

        setFolders(formattedFolders);
      } catch (error: unknown) {
        if (isCancelled) {
          return;
        }

        console.error("Error fetching folders:", error);

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
  }, [token, isAuthLoading]);

  function handleEditClick(folder: DashboardFolder) {
    setFolderToEdit(folder);
    setIsEditFormOpen(true);
  }

  function handleEditClose() {
    setFolderToEdit(null);
    setIsEditFormOpen(false);
  }

  function handleFolderUpdated(updatedFolder: FolderDTO) {
    setFolders((previousFolders) =>
      previousFolders.map((folder) =>
        folder.id === updatedFolder.folderId.toString()
          ? {
              ...folder,
              title: updatedFolder.name,
              description: updatedFolder.description ?? "",
              nickname: updatedFolder.nickname,
              setCount: Number(updatedFolder.setCount),
            }
          : folder,
      ),
    );
  }

  function handleDeleteClick(folder: DashboardFolder) {
    setFolderToDelete(folder);
    setIsDeleteDialogOpen(true);
  }

  function handleDeleteCancel() {
    if (isDeleting) {
      return;
    }

    setFolderToDelete(null);
    setIsDeleteDialogOpen(false);
  }

  async function handleDeleteConfirm() {
    if (!folderToDelete || !token) {
      return;
    }

    try {
      setIsDeleting(true);

      await deleteFolderById(folderToDelete.id, token);

      setFolders((previousFolders) =>
        previousFolders.filter((folder) => folder.id !== folderToDelete.id),
      );

      toast.success("Folder deleted successfully");
    } catch (error: unknown) {
      console.error("Failed to delete folder:", error);

      toast.error("Failed to delete folder. Please try again.");
    } finally {
      setIsDeleting(false);
      setIsDeleteDialogOpen(false);
      setFolderToDelete(null);
    }
  }

  const normalizedSearchQuery = searchQuery.trim().toLowerCase();

  const filteredFolders = folders.filter((folder) =>
    folder.title.toLowerCase().includes(normalizedSearchQuery),
  );

  const isPageLoading = isAuthLoading || isLoading;

  return (
    <>
      <div className="container mx-auto px-4 py-8">
        <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-foreground">Your folders</h1>

            <p className="mt-1 text-sm text-muted-foreground">
              Organize your flashcard sets into folders.
            </p>
          </div>

          <Button className="bg-pink-500 text-white hover:bg-pink-600" asChild>
            <Link href="/profile/folders/create">
              <Plus className="mr-2 h-4 w-4" />
              Create folder
            </Link>
          </Button>
        </div>

        <div className="mb-6">
          <div className="relative max-w-xl">
            <Search
              className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
              aria-hidden="true"
            />

            <Input
              type="search"
              placeholder="Search folders..."
              value={searchQuery}
              onChange={(event) => setSearchQuery(event.target.value)}
              className="border-input bg-background pl-10 text-foreground placeholder:text-muted-foreground focus-visible:border-pink-300 focus-visible:ring-pink-500/20 dark:focus-visible:border-pink-800"
              aria-label="Search folders"
            />
          </div>
        </div>

        {isPageLoading ? (
          <div className="py-10 text-center text-muted-foreground">
            Downloading folders...
          </div>
        ) : !token ? (
          <EmptyFoldersState
            title="You are not logged in"
            description="Log in to see your folders."
          />
        ) : filteredFolders.length > 0 ? (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {filteredFolders.map((folder) => (
              <FolderCard
                key={folder.id}
                folder={folder}
                onEditClick={handleEditClick}
                onDeleteClick={handleDeleteClick}
                isMenuForcedOpen={
                  (isDeleteDialogOpen && folderToDelete?.id === folder.id) ||
                  (isEditFormOpen && folderToEdit?.id === folder.id)
                }
              />
            ))}
          </div>
        ) : (
          <EmptyFoldersState
            title={
              normalizedSearchQuery ? "No folders found" : "No folders yet"
            }
            description={
              normalizedSearchQuery
                ? "Try a different search term."
                : "Create your first folder to organize your flashcard sets."
            }
          />
        )}
      </div>

      <EditFolderForm
        folder={folderToEdit}
        isOpen={isEditFormOpen}
        onClose={handleEditClose}
        onFolderUpdated={handleFolderUpdated}
      />

      <AlertDialog
        open={isDeleteDialogOpen}
        onOpenChange={(open) => {
          if (!isDeleting) {
            setIsDeleteDialogOpen(open);

            if (!open) {
              setFolderToDelete(null);
            }
          }
        }}
      >
        <AlertDialogContent className="border-border bg-background text-foreground">
          <AlertDialogHeader>
            <AlertDialogTitle>Delete folder?</AlertDialogTitle>

            <AlertDialogDescription>
              This action cannot be undone. This will permanently delete the
              folder &quot;{folderToDelete?.title}&quot;. Your flashcard sets
              will not be deleted, only removed from this folder.
            </AlertDialogDescription>
          </AlertDialogHeader>

          <AlertDialogFooter>
            <AlertDialogCancel
              disabled={isDeleting}
              onClick={handleDeleteCancel}
            >
              Cancel
            </AlertDialogCancel>

            <AlertDialogAction
              disabled={isDeleting}
              onClick={() => {
                void handleDeleteConfirm();
              }}
              className="bg-red-500 text-white hover:bg-red-600 dark:bg-red-600 dark:hover:bg-red-700"
            >
              {isDeleting ? "Deleting..." : "Delete"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}

interface FolderCardProps {
  folder: DashboardFolder;
  onEditClick: (folder: DashboardFolder) => void;
  onDeleteClick: (folder: DashboardFolder) => void;
  isMenuForcedOpen: boolean;
}

function FolderCard({
  folder,
  onEditClick,
  onDeleteClick,
  isMenuForcedOpen,
}: FolderCardProps) {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  return (
    <Card className="group border-border bg-card text-card-foreground transition-all hover:border-pink-200 hover:shadow-md dark:hover:border-pink-900">
      <CardContent className="p-4">
        <div className="mb-3 flex items-start gap-3">
          <div className="shrink-0 rounded-lg bg-pink-100 p-2 dark:bg-pink-950/50">
            <FolderOpen className="h-5 w-5 text-pink-500 dark:text-pink-400" />
          </div>

          <div className="min-w-0 flex-1">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <Link
                  href={`/profile/folders/${folder.id}`}
                  className="line-clamp-1 font-semibold text-card-foreground transition-colors hover:text-pink-500 dark:hover:text-pink-400"
                >
                  {folder.title}
                </Link>

                <p className="mt-1 text-sm text-muted-foreground">
                  {folder.setCount} {folder.setCount === 1 ? "set" : "sets"}
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
                      isDropdownOpen || isMenuForcedOpen
                        ? "opacity-100"
                        : "opacity-0 group-hover:opacity-100 group-focus-within:opacity-100",
                    )}
                    aria-label={`Open options for ${folder.title}`}
                  >
                    <MoreHorizontal className="h-4 w-4 text-muted-foreground" />
                  </Button>
                </DropdownMenuTrigger>

                <DropdownMenuContent
                  align="end"
                  className="border-border bg-popover text-popover-foreground"
                >
                  <DropdownMenuItem
                    className="cursor-pointer"
                    onSelect={(event) => {
                      event.preventDefault();
                      onEditClick(folder);
                      setIsDropdownOpen(false);
                    }}
                  >
                    Edit
                  </DropdownMenuItem>

                  <DropdownMenuItem
                    className="cursor-pointer text-red-500 focus:bg-red-50 focus:text-red-600 dark:focus:bg-red-950/40 dark:focus:text-red-400"
                    onSelect={(event) => {
                      event.preventDefault();
                      onDeleteClick(folder);
                      setIsDropdownOpen(false);
                    }}
                  >
                    Delete
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          </div>
        </div>

        {folder.description && (
          <p className="mb-4 line-clamp-2 text-sm text-muted-foreground">
            {folder.description}
          </p>
        )}

        <div className="flex items-center gap-2">
          <Avatar className="h-6 w-6">
            <AvatarFallback className="bg-pink-100 text-xs text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
              <User className="h-3 w-3" aria-hidden="true" />
            </AvatarFallback>
          </Avatar>

          <span className="truncate text-sm text-muted-foreground">
            {folder.nickname}
          </span>
        </div>
      </CardContent>
    </Card>
  );
}

interface EmptyFoldersStateProps {
  title: string;
  description: string;
}

function EmptyFoldersState({ title, description }: EmptyFoldersStateProps) {
  return (
    <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-border bg-card py-16 px-4 text-center text-card-foreground">
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
