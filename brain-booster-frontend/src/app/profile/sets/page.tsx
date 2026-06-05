"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { Plus, Search } from "lucide-react";
import { toast } from "sonner";

import { useAuth } from "@/context/AuthContext";
import { parseJwt } from "@/utils/jwt";

import { getUserFlashcardSetsByUserId } from "@/api/userService";
import { deleteFlashcardSet } from "@/api/flashcardSetService";

import FolderListComponent from "@/app/profile/folders/components/folder-list-component";

import StudySetCard, {
  type StudySetListItem,
} from "@/components/sets/study-set-card";
import EmptySetsState from "@/components/sets/empty-sets-state";

import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
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
    const fetchSets = async () => {
      if (isAuthLoading) return;

      if (!token) {
        setSets([]);
        setIsLoading(false);
        return;
      }

      const decoded = parseJwt(token) as DecodedToken | null;
      const userId = Number(decoded?.id);

      if (!decoded?.id || Number.isNaN(userId)) {
        setSets([]);
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);

        const setsData = await getUserFlashcardSetsByUserId(userId, token);

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
      } catch (error) {
        console.error("Error fetching sets:", error);

        toast.error("Failed to load study sets.", {
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

    fetchSets();
  }, [token, isAuthLoading]);

  const filteredSets = useMemo(() => {
    const normalizedQuery = searchQuery.toLowerCase().trim();

    if (!normalizedQuery) return sets;

    return sets.filter(
      (set) =>
        set.title.toLowerCase().includes(normalizedQuery) ||
        set.description.toLowerCase().includes(normalizedQuery),
    );
  }, [sets, searchQuery]);

  const handleDeleteClick = (set: StudySet) => {
    setSetToDelete(set);
    setIsDeleteDialogOpen(true);
  };

  const handleDeleteCancel = () => {
    if (isDeleting) return;

    setSetToDelete(null);
    setIsDeleteDialogOpen(false);
  };

  const handleDeleteConfirm = async () => {
    if (!setToDelete || !token) return;

    try {
      setIsDeleting(true);

      await deleteFlashcardSet(setToDelete.id, token);

      setSets((prevSets) =>
        prevSets.filter((set) => set.id !== setToDelete.id),
      );

      toast.success("Set deleted successfully", {
        style: {
          background: "green",
          color: "white",
        },
      });
    } catch (error) {
      console.error("Failed to delete set:", error);

      toast.error("Failed to delete set. Please try again.", {
        style: {
          background: "red",
          color: "white",
        },
      });
    } finally {
      setIsDeleting(false);
      setIsDeleteDialogOpen(false);
      setSetToDelete(null);
    }
  };

  const handleAddToFolderClick = (set: StudySet) => {
    setSetToAddToFolder(set);
    setIsFolderListOpen(true);
  };

  const handleFolderListClose = () => {
    setSetToAddToFolder(null);
    setIsFolderListOpen(false);
  };

  const isPageLoading = isAuthLoading || isLoading;

  return (
    <>
      <div className="container mx-auto px-4 py-8">
        <div className="mb-8 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">Your sets</h1>

            <p className="mt-2 text-gray-500">
              Manage your flashcard sets and add them to folders.
            </p>
          </div>

          <Button className="bg-pink-500 text-white hover:bg-pink-600" asChild>
            <Link href="/create-set">
              <Plus className="mr-2 h-4 w-4" />
              Create set
            </Link>
          </Button>
        </div>

        <div className="relative mb-6">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />

          <Input
            value={searchQuery}
            onChange={(event) => setSearchQuery(event.target.value)}
            placeholder="Search your sets..."
            className="border-gray-200 pl-10 focus:border-pink-300 focus:ring-pink-200"
          />
        </div>

        {isPageLoading ? (
          <div className="py-10 text-center text-gray-500">
            Downloading study sets...
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
              searchQuery ? "No sets found" : "You don't have any sets yet"
            }
            description={
              searchQuery
                ? "Try using a different search phrase."
                : "Create your first flashcard set to start learning."
            }
            showCreateButton
          />
        )}
      </div>

      <AlertDialog
        open={isDeleteDialogOpen}
        onOpenChange={setIsDeleteDialogOpen}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete set?</AlertDialogTitle>

            <AlertDialogDescription>
              This action cannot be undone. This will permanently delete the set
              &quot;{setToDelete?.title}&quot; and all flashcards inside it.
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
              onClick={handleDeleteConfirm}
              disabled={isDeleting}
              className="bg-red-500 text-white hover:bg-red-600"
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
          onFolderUpdated={() => {}}
        />
      )}
    </>
  );
}
