"use client";

import type { SubmitEvent } from "react";
import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft, FolderPlus } from "lucide-react";
import { toast } from "sonner";

import { createFolder, type CreateFolderData } from "@/api/folderService";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuth } from "@/context/AuthContext";

export default function CreateFolderForm() {
  const router = useRouter();
  const { token, isAuthLoading } = useAuth();

  const [formData, setFormData] = useState<CreateFolderData>({
    name: "",
    description: "",
  });

  const [isSubmitting, setIsSubmitting] = useState(false);

  function handleChange(field: keyof CreateFolderData, value: string) {
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
      toast.error("You must be logged in to create a folder.");
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

      const createdFolder = await createFolder(
        {
          name: trimmedName,
          description: trimmedDescription,
        },
        token,
      );

      toast.success("Folder created successfully");

      router.push(`/profile/folders/${createdFolder.folderId}`);
    } catch (error: unknown) {
      console.error("Failed to create folder:", error);

      const message =
        error instanceof Error ? error.message : "Failed to create folder.";

      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="container mx-auto max-w-3xl px-4 py-8">
      <div className="mb-6">
        <Button
          variant="ghost"
          size="sm"
          className="text-muted-foreground hover:bg-accent hover:text-pink-500 dark:hover:text-pink-400"
          asChild
        >
          <Link href="/profile/folders">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to folders
          </Link>
        </Button>
      </div>

      <Card className="border-border bg-card text-card-foreground shadow-sm">
        <CardHeader>
          <div className="flex items-center gap-3">
            <div className="shrink-0 rounded-xl bg-pink-100 p-3 dark:bg-pink-950/50">
              <FolderPlus className="h-6 w-6 text-pink-500 dark:text-pink-400" />
            </div>

            <div>
              <CardTitle className="text-2xl font-bold text-card-foreground">
                Create folder
              </CardTitle>

              <p className="mt-1 text-sm text-muted-foreground">
                Organize your flashcard sets into a new folder.
              </p>
            </div>
          </div>
        </CardHeader>

        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="space-y-2">
              <Label htmlFor="name" className="text-foreground">
                Folder name
              </Label>

              <Input
                id="name"
                type="text"
                placeholder="e.g. Biology, English vocabulary, Exam prep"
                value={formData.name}
                onChange={(event) => handleChange("name", event.target.value)}
                disabled={isSubmitting}
                autoFocus
                className="border-input bg-background text-foreground placeholder:text-muted-foreground focus-visible:border-pink-300 focus-visible:ring-pink-500/20 dark:focus-visible:border-pink-800"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="description" className="text-foreground">
                Description
              </Label>

              <textarea
                id="description"
                placeholder="Describe what this folder is for..."
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
                className="border-border"
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
