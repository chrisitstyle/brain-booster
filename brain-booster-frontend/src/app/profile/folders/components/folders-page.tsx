"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { FolderOpen, MoreHorizontal, Plus, Search, User } from "lucide-react";
import { toast } from "sonner";

import { useAuth } from "@/context/AuthContext";
import {
  deleteFolderById,
  getMyFolders,
  type Folder as FolderDTO,
} from "@/api/folderService";

import EditFolderForm from "@/app/profile/folders/components/edit-folder-form";

import { Input } from "@/components/ui/input";
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
    const fetchFolders = async () => {
      if (isAuthLoading) return;

      if (!token) {
        setFolders([]);
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);

        const foldersData = await getMyFolders(token);

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
      } catch (error) {
        console.error("Error fetching folders:", error);

        toast.error("Failed to load folders.", {
          style: {
            background: "red",
            color: "white",
          },
        });

        setFolders([]);
      } finally {
        setIsLoading(false);
      }
    };

    fetchFolders();
  }, [token, isAuthLoading]);

  const handleEditClick = (folder: DashboardFolder) => {
    setFolderToEdit(folder);
    setIsEditFormOpen(true);
  };

  const handleEditClose = () => {
    setFolderToEdit(null);
    setIsEditFormOpen(false);
  };

  const handleFolderUpdated = (updatedFolder: FolderDTO) => {
    setFolders((prevFolders) =>
      prevFolders.map((folder) =>
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
  };

  const handleDeleteClick = (folder: DashboardFolder) => {
    setFolderToDelete(folder);
    setIsDeleteDialogOpen(true);
  };

  const handleDeleteCancel = () => {
    if (isDeleting) return;

    setFolderToDelete(null);
    setIsDeleteDialogOpen(false);
  };

  const handleDeleteConfirm = async () => {
    if (!folderToDelete || !token) return;

    try {
      setIsDeleting(true);

      await deleteFolderById(folderToDelete.id, token);

      setFolders((prevFolders) =>
        prevFolders.filter((folder) => folder.id !== folderToDelete.id),
      );

      toast.success("Folder deleted successfully", {
        style: {
          background: "green",
          color: "white",
        },
      });
    } catch (error) {
      console.error("Failed to delete folder:", error);

      toast.error("Failed to delete folder. Please try again.", {
        style: {
          background: "red",
          color: "white",
        },
      });
    } finally {
      setIsDeleting(false);
      setIsDeleteDialogOpen(false);
      setFolderToDelete(null);
    }
  };

  const filteredFolders = folders.filter((folder) =>
    folder.title.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  const isPageLoading = isAuthLoading || isLoading;

  return (
    <>
      <div className="container mx-auto px-4 py-8">
        <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">Your folders</h1>
            <p className="mt-1 text-sm text-gray-500">
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
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />

            <Input
              type="text"
              placeholder="Search folders..."
              value={searchQuery}
              onChange={(event) => setSearchQuery(event.target.value)}
              className="border-gray-200 pl-10 focus:border-pink-300 focus:ring-pink-200"
            />
          </div>
        </div>

        {isPageLoading ? (
          <div className="py-10 text-center text-gray-500">
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
            title={searchQuery ? "No folders found" : "No folders yet"}
            description={
              searchQuery
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
        onOpenChange={setIsDeleteDialogOpen}
      >
        <AlertDialogContent>
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
              onClick={handleDeleteConfirm}
              className="bg-red-500 text-white hover:bg-red-600"
            >
              {isDeleting ? "Deleting..." : "Delete"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}

function FolderCard({
  folder,
  onEditClick,
  onDeleteClick,
  isMenuForcedOpen,
}: {
  folder: DashboardFolder;
  onEditClick: (folder: DashboardFolder) => void;
  onDeleteClick: (folder: DashboardFolder) => void;
  isMenuForcedOpen: boolean;
}) {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  return (
    <Card className="group cursor-pointer border-gray-200 bg-white transition-all hover:border-pink-200 hover:shadow-md">
      <CardContent className="p-4">
        <div className="mb-3 flex items-start gap-3">
          <div className="rounded-lg bg-pink-100 p-2">
            <FolderOpen className="h-5 w-5 text-pink-500" />
          </div>

          <div className="min-w-0 flex-1">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <Link
                  href={`/profile/folders/${folder.id}`}
                  className="line-clamp-1 font-semibold text-gray-800 hover:text-pink-500"
                >
                  {folder.title}
                </Link>

                <p className="mt-1 text-sm text-gray-500">
                  {folder.setCount} {folder.setCount === 1 ? "set" : "sets"}
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
                      isDropdownOpen || isMenuForcedOpen
                        ? "h-8 w-8 opacity-100"
                        : "h-8 w-8 opacity-0 transition-opacity group-hover:opacity-100"
                    }
                  >
                    <MoreHorizontal className="h-4 w-4 text-gray-400" />
                  </Button>
                </DropdownMenuTrigger>

                <DropdownMenuContent align="end">
                  <DropdownMenuItem
                    onSelect={(event) => {
                      event.preventDefault();
                      onEditClick(folder);
                      setIsDropdownOpen(false);
                    }}
                  >
                    Edit
                  </DropdownMenuItem>

                  <DropdownMenuItem
                    className="cursor-pointer text-red-500"
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
          <p className="mb-4 line-clamp-2 text-sm text-gray-600">
            {folder.description}
          </p>
        )}

        <div className="flex items-center gap-2">
          <Avatar className="h-6 w-6">
            <AvatarFallback className="bg-pink-100 text-xs text-pink-500">
              <User className="h-3 w-3" />
            </AvatarFallback>
          </Avatar>

          <span className="text-sm text-gray-500">{folder.nickname}</span>
        </div>
      </CardContent>
    </Card>
  );
}

function EmptyFoldersState({
  title,
  description,
}: {
  title: string;
  description: string;
}) {
  return (
    <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-gray-200 bg-white py-16 text-center">
      <div className="mb-4 rounded-full bg-gray-100 p-4">
        <FolderOpen className="h-8 w-8 text-gray-400" />
      </div>

      <h2 className="text-lg font-semibold text-gray-800">{title}</h2>
      <p className="mt-1 max-w-sm text-sm text-gray-500">{description}</p>
    </div>
  );
}
