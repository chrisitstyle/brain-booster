"use client";

import type { SubmitEvent } from "react";
import { useState } from "react";
import { FolderPen, X } from "lucide-react";
import { toast } from "sonner";

import {
  editFolder,
  type EditFolderData,
  type Folder,
} from "@/api/folderService";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuth } from "@/context/AuthContext";

interface EditableFolder {
  id: string;
  title: string;
  description: string;
}

interface EditFolderFormProps {
  folder: EditableFolder | null;
  isOpen: boolean;
  onClose: () => void;
  onFolderUpdated: (folder: Folder) => void;
}

interface EditFolderFormContentProps {
  folder: EditableFolder;
  onClose: () => void;
  onFolderUpdated: (folder: Folder) => void;
}

export default function EditFolderForm({
  folder,
  isOpen,
  onClose,
  onFolderUpdated,
}: EditFolderFormProps) {
  if (!isOpen || !folder) {
    return null;
  }

  return (
    <EditFolderFormContent
      key={folder.id}
      folder={folder}
      onClose={onClose}
      onFolderUpdated={onFolderUpdated}
    />
  );
}

function EditFolderFormContent({
  folder,
  onClose,
  onFolderUpdated,
}: EditFolderFormContentProps) {
  const { token, isAuthLoading } = useAuth();

  const [formData, setFormData] = useState<EditFolderData>({
    folderId: folder.id,
    name: folder.title,
    description: folder.description ?? "",
  });

  const [isSubmitting, setIsSubmitting] = useState(false);

  function handleChange(field: "name" | "description", value: string) {
    setFormData((previousFormData) => ({
      ...previousFormData,
      [field]: value,
    }));
  }

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();

    if (isAuthLoading) {
      return;
    }

    if (!token) {
      toast.error("You must be logged in to edit a folder.");
      return;
    }

    const trimmedName = formData.name.trim();
    const trimmedDescription = formData.description.trim();

    if (!trimmedName) {
      toast.error("Folder name is required.");
      return;
    }

    try {
      setIsSubmitting(true);

      const updatedFolder = await editFolder(
        {
          folderId: formData.folderId,
          name: trimmedName,
          description: trimmedDescription,
        },
        token,
      );

      onFolderUpdated(updatedFolder);

      toast.success("Folder updated successfully");

      onClose();
    } catch (error: unknown) {
      console.error("Failed to edit folder:", error);

      const message =
        error instanceof Error ? error.message : "Failed to edit folder.";

      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="edit-folder-title"
      aria-describedby="edit-folder-description"
    >
      <Card className="w-full max-w-2xl border-border bg-card text-card-foreground shadow-xl">
        <div className="flex items-start justify-between gap-4 border-b border-border p-5">
          <div className="flex min-w-0 items-center gap-3">
            <div className="shrink-0 rounded-xl bg-pink-100 p-3 dark:bg-pink-950/50">
              <FolderPen className="h-6 w-6 text-pink-500 dark:text-pink-400" />
            </div>

            <div className="min-w-0">
              <h2
                id="edit-folder-title"
                className="text-xl font-bold text-card-foreground"
              >
                Edit folder
              </h2>

              <p
                id="edit-folder-description"
                className="mt-1 text-sm text-muted-foreground"
              >
                Update folder name and description.
              </p>
            </div>
          </div>

          <Button
            type="button"
            variant="ghost"
            size="icon"
            disabled={isSubmitting}
            onClick={onClose}
            className="shrink-0 text-muted-foreground hover:bg-accent hover:text-foreground"
            aria-label="Close edit folder form"
          >
            <X className="h-5 w-5" aria-hidden="true" />
          </Button>
        </div>

        <CardContent className="p-5">
          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="space-y-2">
              <Label htmlFor="folder-name" className="text-foreground">
                Folder name
              </Label>

              <Input
                id="folder-name"
                type="text"
                value={formData.name}
                onChange={(event) => handleChange("name", event.target.value)}
                disabled={isSubmitting}
                autoFocus
                className="border-input bg-background text-foreground placeholder:text-muted-foreground focus-visible:border-pink-300 focus-visible:ring-pink-500/20 dark:focus-visible:border-pink-800"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="folder-description" className="text-foreground">
                Description
              </Label>

              <textarea
                id="folder-description"
                value={formData.description}
                onChange={(event) =>
                  handleChange("description", event.target.value)
                }
                disabled={isSubmitting}
                rows={5}
                className="flex w-full resize-y rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground shadow-sm outline-none transition-colors placeholder:text-muted-foreground focus-visible:border-pink-300 focus-visible:ring-2 focus-visible:ring-pink-500/20 disabled:cursor-not-allowed disabled:opacity-50 dark:focus-visible:border-pink-800"
              />
            </div>

            <div className="flex flex-col-reverse gap-3 pt-2 sm:flex-row sm:justify-end">
              <Button
                type="button"
                variant="outline"
                disabled={isSubmitting}
                onClick={onClose}
                className="border-border"
              >
                Cancel
              </Button>

              <Button
                type="submit"
                disabled={isSubmitting || isAuthLoading}
                className="bg-pink-500 text-white hover:bg-pink-600"
              >
                {isSubmitting ? "Saving..." : "Save changes"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
