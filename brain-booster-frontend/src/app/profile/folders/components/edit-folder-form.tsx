"use client";

import { useState } from "react";
import { FolderPen, X } from "lucide-react";
import { toast } from "sonner";

import { useAuth } from "@/context/AuthContext";
import {
  editFolder,
  type EditFolderData,
  type Folder,
} from "@/api/folderService";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/label";

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
}: {
  folder: EditableFolder;
  onClose: () => void;
  onFolderUpdated: (folder: Folder) => void;
}) {
  const { token, isAuthLoading } = useAuth();

  const [formData, setFormData] = useState<EditFolderData>({
    folderId: folder.id,
    name: folder.title,
    description: folder.description ?? "",
  });

  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (field: "name" | "description", value: string) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleSubmit = async (event: React.SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (isAuthLoading) return;

    if (!token) {
      toast.error("You must be logged in to edit a folder.", {
        style: {
          background: "red",
          color: "white",
        },
      });

      return;
    }

    const trimmedName = formData.name.trim();
    const trimmedDescription = formData.description.trim();

    if (!trimmedName) {
      toast.error("Folder name is required.", {
        style: {
          background: "red",
          color: "white",
        },
      });

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

      toast.success("Folder updated successfully", {
        style: {
          background: "green",
          color: "white",
        },
      });

      onClose();
    } catch (error) {
      console.error("Failed to edit folder:", error);

      const message =
        error instanceof Error ? error.message : "Failed to edit folder.";

      toast.error(message, {
        style: {
          background: "red",
          color: "white",
        },
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
      <Card className="w-full max-w-2xl border-gray-200 bg-white shadow-xl">
        <div className="flex items-start justify-between border-b border-gray-100 p-5">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-pink-100 p-3">
              <FolderPen className="h-6 w-6 text-pink-500" />
            </div>

            <div>
              <h2 className="text-xl font-bold text-gray-800">Edit folder</h2>
              <p className="mt-1 text-sm text-gray-500">
                Update folder name and description.
              </p>
            </div>
          </div>

          <Button
            variant="ghost"
            size="icon"
            disabled={isSubmitting}
            onClick={onClose}
          >
            <X className="h-5 w-5 text-gray-500" />
          </Button>
        </div>

        <CardContent className="p-5">
          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="space-y-2">
              <Label htmlFor="folder-name">Folder name</Label>
              <Input
                id="folder-name"
                type="text"
                value={formData.name}
                onChange={(event) => handleChange("name", event.target.value)}
                disabled={isSubmitting}
                className="border-gray-200 focus:border-pink-300 focus:ring-pink-200"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="folder-description">Description</Label>
              <textarea
                id="folder-description"
                value={formData.description}
                onChange={(event) =>
                  handleChange("description", event.target.value)
                }
                disabled={isSubmitting}
                rows={5}
                className="flex w-full rounded-md border border-gray-200 bg-transparent px-3 py-2 text-sm shadow-sm outline-none placeholder:text-gray-400 focus:border-pink-300 focus:ring-1 focus:ring-pink-200 disabled:cursor-not-allowed disabled:opacity-50"
              />
            </div>

            <div className="flex flex-col-reverse gap-3 pt-2 sm:flex-row sm:justify-end">
              <Button
                type="button"
                variant="outline"
                disabled={isSubmitting}
                onClick={onClose}
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
