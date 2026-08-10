import type { Dispatch, SetStateAction } from "react";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";

import { deleteFlashcardSet } from "@/api/flashcardSetService";
import {
  deleteFolderById,
  type Folder as FolderDTO,
} from "@/api/folderService";
import type {
  DashboardFolder,
  StudySet,
} from "@/app/profile/hooks/use-profile-dashboard-data";

interface UseProfileDashboardActionsParams {
  readonly token: string | null;
  readonly setSets: Dispatch<SetStateAction<StudySet[]>>;
  readonly setFolders: Dispatch<SetStateAction<DashboardFolder[]>>;
}

export function useProfileDashboardActions({
  token,
  setSets,
  setFolders,
}: UseProfileDashboardActionsParams) {
  const router = useRouter();

  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [setToDelete, setSetToDelete] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const [isFolderDeleteDialogOpen, setIsFolderDeleteDialogOpen] =
    useState(false);

  const [folderToDelete, setFolderToDelete] = useState<DashboardFolder | null>(
    null,
  );

  const [isDeletingFolder, setIsDeletingFolder] = useState(false);

  const [isEditFolderFormOpen, setIsEditFolderFormOpen] = useState(false);
  const [folderToEdit, setFolderToEdit] = useState<DashboardFolder | null>(
    null,
  );

  const [isFolderListOpen, setIsFolderListOpen] = useState(false);
  const [setToAddToFolder, setSetToAddToFolder] = useState<StudySet | null>(
    null,
  );

  function handleEditSetClick(set: StudySet) {
    router.push(`/users/${encodeURIComponent(set.author)}/sets/${set.id}/edit`);
  }

  function handleDeleteSetClick(setId: string) {
    setSetToDelete(setId);
    setIsDeleteDialogOpen(true);
  }

  function handleAddToFolderClick(set: StudySet) {
    setSetToAddToFolder(set);
    setIsFolderListOpen(true);
  }

  function handleFolderListClose() {
    setSetToAddToFolder(null);
    setIsFolderListOpen(false);
  }

  function handleFolderUpdated(updatedFolder: FolderDTO) {
    setFolders((previousFolders) =>
      previousFolders.map((folder) =>
        folder.id === updatedFolder.folderId.toString()
          ? {
              ...folder,
              title: updatedFolder.name,
              description: updatedFolder.description ?? "",
              setCount: updatedFolder.setCount,
            }
          : folder,
      ),
    );
  }

  async function handleDeleteConfirm() {
    if (!setToDelete || !token) {
      return;
    }

    try {
      setIsDeleting(true);

      await deleteFlashcardSet(setToDelete, token);

      setSets((previousSets) =>
        previousSets.filter((set) => set.id !== setToDelete),
      );

      toast.success("Set deleted successfully");
    } catch (error: unknown) {
      console.error("Failed to delete set:", error);

      toast.error("Failed to delete set. Please try again.");
    } finally {
      setIsDeleting(false);
      setIsDeleteDialogOpen(false);
      setSetToDelete(null);
    }
  }

  function handleDeleteCancel() {
    setSetToDelete(null);
    setIsDeleteDialogOpen(false);
  }

  function handleFolderEditClick(folder: DashboardFolder) {
    setFolderToEdit(folder);
    setIsEditFolderFormOpen(true);
  }

  function handleFolderEditClose() {
    setFolderToEdit(null);
    setIsEditFolderFormOpen(false);
  }

  function handleFolderDeleteClick(folder: DashboardFolder) {
    setFolderToDelete(folder);
    setIsFolderDeleteDialogOpen(true);
  }

  function handleFolderDeleteCancel() {
    if (isDeletingFolder) {
      return;
    }

    setFolderToDelete(null);
    setIsFolderDeleteDialogOpen(false);
  }

  async function handleFolderDeleteConfirm() {
    if (!folderToDelete || !token) {
      return;
    }

    try {
      setIsDeletingFolder(true);

      await deleteFolderById(folderToDelete.id, token);

      setFolders((previousFolders) =>
        previousFolders.filter((folder) => folder.id !== folderToDelete.id),
      );

      toast.success("Folder deleted successfully");
    } catch (error: unknown) {
      console.error("Failed to delete folder:", error);

      toast.error("Failed to delete folder. Please try again.");
    } finally {
      setIsDeletingFolder(false);
      setIsFolderDeleteDialogOpen(false);
      setFolderToDelete(null);
    }
  }

  return {
    isDeleteDialogOpen,
    setIsDeleteDialogOpen,
    setToDelete,
    isDeleting,
    isFolderDeleteDialogOpen,
    setIsFolderDeleteDialogOpen,
    folderToDelete,
    isDeletingFolder,
    isEditFolderFormOpen,
    folderToEdit,
    isFolderListOpen,
    setToAddToFolder,
    handleEditSetClick,
    handleDeleteSetClick,
    handleAddToFolderClick,
    handleFolderListClose,
    handleFolderUpdated,
    handleDeleteConfirm,
    handleDeleteCancel,
    handleFolderEditClick,
    handleFolderEditClose,
    handleFolderDeleteClick,
    handleFolderDeleteCancel,
    handleFolderDeleteConfirm,
  };
}
