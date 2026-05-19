"use client";

import { useEffect, useState } from "react";
import { FolderOpen, Search, X } from "lucide-react";
import { toast } from "sonner";

import { useAuth } from "@/context/AuthContext";
import { addSetToFolder, getMyFolders, type Folder } from "@/api/folderService";

import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

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
    const fetchFolders = async () => {
      if (!isOpen || isAuthLoading) return;

      if (!token) {
        setFolders([]);
        return;
      }

      try {
        setIsLoading(true);

        const data = await getMyFolders(token);

        setFolders(data);
      } catch (error) {
        console.error("Failed to fetch folders:", error);

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
  }, [isOpen, token, isAuthLoading]);

  if (!isOpen) {
    return null;
  }

  const filteredFolders = folders.filter((folder) =>
    folder.name.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  const handleAddToFolder = async (folder: Folder) => {
    if (!token) {
      toast.error("You must be logged in to add sets to folders.", {
        style: {
          background: "red",
          color: "white",
        },
      });

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

      setFolders((prevFolders) =>
        prevFolders.map((currentFolder) =>
          currentFolder.folderId === updatedFolder.folderId
            ? updatedFolder
            : currentFolder,
        ),
      );

      onFolderUpdated?.(updatedFolder);

      toast.success("Set added to folder", {
        style: {
          background: "green",
          color: "white",
        },
      });
    } catch (error) {
      console.error("Failed to add set to folder:", error);

      const message =
        error instanceof Error ? error.message : "Failed to add set to folder.";

      toast.error(message, {
        style: {
          background: "red",
          color: "white",
        },
      });
    } finally {
      setAddingFolderId(null);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
      <Card className="max-h-[85vh] w-full max-w-2xl overflow-hidden border-gray-200 bg-white shadow-xl">
        <div className="flex items-start justify-between border-b border-gray-100 p-5">
          <div>
            <h2 className="text-xl font-bold text-gray-800">Add to folder</h2>
            <p className="mt-1 text-sm text-gray-500">{flashcardSetTitle}</p>
          </div>

          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="h-5 w-5 text-gray-500" />
          </Button>
        </div>

        <CardContent className="p-5">
          <div className="mb-5">
            <div className="relative">
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

          {isLoading || isAuthLoading ? (
            <div className="py-10 text-center text-gray-500">
              Downloading folders...
            </div>
          ) : !token ? (
            <div className="py-10 text-center text-gray-500">
              You must be logged in to add sets to folders.
            </div>
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
                    className="flex items-center justify-between gap-4 rounded-lg border border-gray-200 bg-white p-4 transition-colors hover:border-pink-200"
                  >
                    <div className="flex min-w-0 items-start gap-3">
                      <div className="rounded-lg bg-pink-50 p-2">
                        <FolderOpen className="h-5 w-5 text-pink-500" />
                      </div>

                      <div className="min-w-0">
                        <h3 className="line-clamp-1 font-semibold text-gray-800">
                          {folder.name}
                        </h3>

                        {folder.description && (
                          <p className="mt-1 line-clamp-2 text-sm text-gray-500">
                            {folder.description}
                          </p>
                        )}

                        <p className="mt-1 text-xs font-medium uppercase tracking-wide text-gray-400">
                          {folder.setCount}{" "}
                          {folder.setCount === 1 ? "set" : "sets"}
                        </p>
                      </div>
                    </div>

                    <Button
                      size="sm"
                      disabled={isAlreadyInFolder || isAdding}
                      onClick={() => handleAddToFolder(folder)}
                      className="shrink-0 bg-pink-500 text-white hover:bg-pink-600 disabled:bg-gray-200 disabled:text-gray-500"
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
            <div className="py-10 text-center text-gray-500">
              {searchQuery
                ? "No folders found for this search."
                : "You do not have any folders yet."}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
