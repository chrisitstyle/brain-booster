"use client";

import { useEffect, useState } from "react";
import { BookOpen, Search, X } from "lucide-react";
import { toast } from "sonner";

import { useAuth } from "@/context/AuthContext";
import { parseJwt } from "@/utils/jwt";
import { getUserFlashcardSetsByUserId } from "@/api/userService";
import {
  addSetToFolder,
  type FlashcardSetInFolder,
  type Folder,
} from "@/api/folderService";

import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

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
    const fetchUserSets = async () => {
      if (!isOpen || isAuthLoading) return;

      if (!token) {
        setSets([]);
        return;
      }

      const decoded = parseJwt(token);

      if (!decoded?.id) {
        setSets([]);
        return;
      }

      try {
        setIsLoading(true);

        const data = await getUserFlashcardSetsByUserId(decoded.id, token);

        setSets(data);
      } catch (error) {
        console.error("Failed to fetch user sets:", error);

        toast.error("Failed to load your sets.", {
          style: {
            background: "red",
            color: "white",
          },
        });

        setSets([]);
      } finally {
        setIsLoading(false);
      }
    };

    fetchUserSets();
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

  const filteredSets = setsNotInFolder.filter((set) =>
    set.setName.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  const handleAddSet = async (set: FlashcardSetDTO) => {
    if (!token) {
      toast.error("You must be logged in to add sets to folder.", {
        style: {
          background: "red",
          color: "white",
        },
      });

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
      setAddingSetId(null);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
      <Card className="max-h-[85vh] w-full max-w-2xl overflow-hidden border-gray-200 bg-white shadow-xl">
        <div className="flex items-start justify-between border-b border-gray-100 p-5">
          <div>
            <h2 className="text-xl font-bold text-gray-800">
              Add set to folder
            </h2>
            <p className="mt-1 text-sm text-gray-500">{folderName}</p>
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
                placeholder="Search your sets..."
                value={searchQuery}
                onChange={(event) => setSearchQuery(event.target.value)}
                className="border-gray-200 pl-10 focus:border-pink-300 focus:ring-pink-200"
              />
            </div>
          </div>

          {isLoading || isAuthLoading ? (
            <div className="py-10 text-center text-gray-500">
              Downloading sets...
            </div>
          ) : !token ? (
            <div className="py-10 text-center text-gray-500">
              You must be logged in to add sets.
            </div>
          ) : filteredSets.length > 0 ? (
            <div className="max-h-[50vh] space-y-3 overflow-y-auto pr-1">
              {filteredSets.map((set) => {
                const isAdding = addingSetId === set.setId;

                return (
                  <div
                    key={set.setId}
                    className="flex items-center justify-between gap-4 rounded-lg border border-gray-200 bg-white p-4 transition-colors hover:border-pink-200"
                  >
                    <div className="flex min-w-0 items-start gap-3">
                      <div className="rounded-lg bg-pink-50 p-2">
                        <BookOpen className="h-5 w-5 text-pink-500" />
                      </div>

                      <div className="min-w-0">
                        <h3 className="line-clamp-1 font-semibold text-gray-800">
                          {set.setName}
                        </h3>

                        {set.description && (
                          <p className="mt-1 line-clamp-2 text-sm text-gray-500">
                            {set.description}
                          </p>
                        )}

                        <p className="mt-1 text-xs font-medium uppercase tracking-wide text-gray-400">
                          {set.termCount}{" "}
                          {set.termCount === 1 ? "term" : "terms"}
                        </p>
                      </div>
                    </div>

                    <Button
                      size="sm"
                      disabled={isAdding}
                      onClick={() => handleAddSet(set)}
                      className="shrink-0 bg-pink-500 text-white hover:bg-pink-600"
                    >
                      {isAdding ? "Adding..." : "Add"}
                    </Button>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="py-10 text-center text-gray-500">
              {searchQuery
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
