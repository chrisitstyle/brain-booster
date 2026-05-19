"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, FolderPlus } from "lucide-react";
import { toast } from "sonner";

import { useAuth } from "@/context/AuthContext";
import { createFolder, type CreateFolderData } from "@/api/folderService";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";

export default function CreateFolderForm() {
  const router = useRouter();
  const { token, isAuthLoading } = useAuth();

  const [formData, setFormData] = useState<CreateFolderData>({
    name: "",
    description: "",
  });

  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (field: keyof CreateFolderData, value: string) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleSubmit = async (event: React.SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (isAuthLoading) return;

    if (!token) {
      toast.error("You must be logged in to create a folder.", {
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

      const createdFolder = await createFolder(
        {
          name: trimmedName,
          description: trimmedDescription,
        },
        token,
      );

      toast.success("Folder created successfully", {
        style: {
          background: "green",
          color: "white",
        },
      });

      router.push(`/profile/folders/${createdFolder.folderId}`);
    } catch (error) {
      console.error("Failed to create folder:", error);

      const message =
        error instanceof Error ? error.message : "Failed to create folder.";

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
    <div className="container mx-auto max-w-3xl px-4 py-8">
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

      <Card className="border-gray-200 bg-white shadow-sm">
        <CardHeader>
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-pink-100 p-3">
              <FolderPlus className="h-6 w-6 text-pink-500" />
            </div>

            <div>
              <CardTitle className="text-2xl font-bold text-gray-800">
                Create folder
              </CardTitle>
              <p className="mt-1 text-sm text-gray-500">
                Organize your flashcard sets into a new folder.
              </p>
            </div>
          </div>
        </CardHeader>

        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="space-y-2">
              <Label htmlFor="name">Folder name</Label>
              <Input
                id="name"
                type="text"
                placeholder="e.g. Biology, English vocabulary, Exam prep"
                value={formData.name}
                onChange={(event) => handleChange("name", event.target.value)}
                disabled={isSubmitting}
                className="border-gray-200 focus:border-pink-300 focus:ring-pink-200"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <textarea
                id="description"
                placeholder="Describe what this folder is for..."
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
                asChild
              >
                <Link href="/profile/folders">Cancel</Link>
              </Button>

              <Button
                type="submit"
                disabled={isSubmitting || isAuthLoading}
                className="bg-pink-500 text-white hover:bg-pink-600"
              >
                {isSubmitting ? "Creating..." : "Create folder"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
