"use client";

import {
  BookOpen,
  Clock,
  Flame,
  FolderOpen,
  Plus,
  Settings,
  Trophy,
  Zap,
} from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { toast } from "sonner";

import { deleteFlashcardSet } from "@/api/flashcardSetService";
import {
  deleteFolderById,
  type Folder as FolderDTO,
} from "@/api/folderService";
import { FolderCard } from "@/app/profile/components/folder-card";
import { StudySetCard } from "@/app/profile/components/study-set-card";
import { RecentActivityItem } from "@/app/profile/components/recent-activity-item";
import EditFolderForm from "@/app/profile/folders/components/edit-folder-form";
import FolderListComponent from "@/app/profile/folders/components/folder-list-component";
import {
  type DashboardFolder,
  type StudySet,
  useProfileDashboardData,
} from "@/app/profile/hooks/use-profile-dashboard-data";
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
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useAuth } from "@/context/AuthContext";
import { cn } from "@/lib/utils";

const achievements = [
  {
    icon: Flame,
    label: "7 Day Streak",
    value: "7",
    color: "text-orange-500",
  },
  {
    icon: Trophy,
    label: "Sets Mastered",
    value: "12",
    color: "text-yellow-500",
  },
  {
    icon: Zap,
    label: "Terms Learned",
    value: "847",
    color: "text-pink-500",
  },
];

export function ProfileDashboard() {
  const router = useRouter();
  const { token } = useAuth();

  const { currentUser, sets, setSets, folders, setFolders, isContentLoading } =
    useProfileDashboardData(token);

  const [activeTab, setActiveTab] = useState("sets");

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

  const avatarFallback =
    currentUser?.nickname.trim().charAt(0).toUpperCase() || "?";

  const memberSince = currentUser?.createdAt
    ? new Intl.DateTimeFormat("en-US", {
        month: "long",
        year: "numeric",
      }).format(new Date(currentUser.createdAt))
    : "";

  function handleEditSetClick(set: StudySet) {
    router.push(`/users/${encodeURIComponent(set.author)}/sets/${set.id}/edit`);
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

  return (
    <>
      <div className="container mx-auto px-4 py-8">
        <div className="mb-8 flex flex-col items-start gap-6 md:flex-row md:items-center md:justify-between">
          <div className="flex min-w-0 items-center gap-4">
            <Avatar className="h-20 w-20 shrink-0 border-4 border-pink-200 dark:border-pink-900">
              <AvatarFallback className="bg-pink-100 text-2xl font-medium text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
                {isContentLoading && !currentUser ? "" : avatarFallback}
              </AvatarFallback>
            </Avatar>

            {currentUser ? (
              <div className="min-w-0">
                <h1 className="truncate text-2xl font-bold text-foreground">
                  {currentUser.nickname}
                </h1>

                <p className="truncate text-muted-foreground">
                  {currentUser.email}
                </p>

                {memberSince && (
                  <p className="mt-1 text-sm text-muted-foreground">
                    Member since {memberSince}
                  </p>
                )}
              </div>
            ) : isContentLoading ? (
              <div className="space-y-2">
                <div className="h-7 w-36 animate-pulse rounded bg-muted" />
                <div className="h-5 w-48 animate-pulse rounded bg-muted" />
                <div className="h-4 w-32 animate-pulse rounded bg-muted" />
              </div>
            ) : (
              <div>
                <h1 className="text-xl font-semibold text-foreground">
                  Profile unavailable
                </h1>

                <p className="text-sm text-muted-foreground">
                  User information could not be loaded.
                </p>
              </div>
            )}
          </div>

          <div className="flex gap-3">
            <Button
              variant="outline"
              size="sm"
              className="border-border text-muted-foreground hover:border-pink-200 hover:bg-pink-50 hover:text-pink-500 dark:hover:border-pink-900 dark:hover:bg-pink-950/40 dark:hover:text-pink-400"
              asChild
            >
              <Link href="/profile/settings">
                <Settings className="mr-2 h-4 w-4" />
                Settings
              </Link>
            </Button>

            <Button
              size="sm"
              className="bg-pink-500 text-white hover:bg-pink-600"
              asChild
            >
              <Link href="/create-set">
                <Plus className="mr-2 h-4 w-4" />
                Create set
              </Link>
            </Button>
          </div>
        </div>

        <div className="mb-8 grid gap-4 md:grid-cols-3">
          {achievements.map((achievement) => (
            <Card
              key={achievement.label}
              className="border-border bg-card text-card-foreground"
            >
              <CardContent className="flex items-center gap-4 p-6">
                <div
                  className={cn("rounded-full bg-muted p-3", achievement.color)}
                >
                  <achievement.icon className="h-6 w-6" />
                </div>

                <div>
                  <p className="text-2xl font-bold text-card-foreground">
                    {achievement.value}
                  </p>

                  <p className="text-sm text-muted-foreground">
                    {achievement.label}
                  </p>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

        <Card className="mb-8 border-border bg-card text-card-foreground">
          <CardHeader className="pb-2">
            <CardTitle className="text-lg font-semibold text-card-foreground">
              Weekly Progress
            </CardTitle>
          </CardHeader>

          <CardContent>
            <div className="mb-2 flex items-center justify-between">
              <span className="text-sm text-muted-foreground">
                5 of 7 days studied
              </span>

              <span className="text-sm font-medium text-pink-500 dark:text-pink-400">
                71%
              </span>
            </div>

            <Progress value={71} className="h-2 bg-muted" />

            <div className="mt-4 flex justify-between">
              {["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"].map(
                (day, index) => (
                  <div key={day} className="flex flex-col items-center gap-2">
                    <div
                      className={cn(
                        "flex h-8 w-8 items-center justify-center rounded-full text-xs font-medium",
                        index < 5
                          ? "bg-pink-500 text-white"
                          : "bg-muted text-muted-foreground",
                      )}
                    >
                      {index < 5 ? <Flame className="h-4 w-4" /> : null}
                    </div>

                    <span className="text-xs text-muted-foreground">{day}</span>
                  </div>
                ),
              )}
            </div>
          </CardContent>
        </Card>

        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
          <TabsList className="mb-6 w-full justify-start gap-2 border-b border-border bg-transparent p-0">
            <TabsTrigger
              value="sets"
              className={cn(
                "rounded-none border-0 border-b-2 border-transparent bg-transparent px-4 py-3 text-muted-foreground shadow-none",
                "data-[state=active]:border-b-pink-500 data-[state=active]:bg-transparent data-[state=active]:text-pink-500 data-[state=active]:shadow-none",
                "dark:data-[state=active]:text-pink-400",
                "focus-visible:ring-0 focus-visible:ring-offset-0",
              )}
            >
              <BookOpen className="mr-2 h-4 w-4" />
              Study Sets
            </TabsTrigger>

            <TabsTrigger
              value="folders"
              className={cn(
                "rounded-none border-0 border-b-2 border-transparent bg-transparent px-4 py-3 text-muted-foreground shadow-none",
                "data-[state=active]:border-b-pink-500 data-[state=active]:bg-transparent data-[state=active]:text-pink-500 data-[state=active]:shadow-none",
                "dark:data-[state=active]:text-pink-400",
                "focus-visible:ring-0 focus-visible:ring-offset-0",
              )}
            >
              <FolderOpen className="mr-2 h-4 w-4" />
              Folders
            </TabsTrigger>

            <TabsTrigger
              value="recent"
              className={cn(
                "rounded-none border-0 border-b-2 border-transparent bg-transparent px-4 py-3 text-muted-foreground shadow-none",
                "data-[state=active]:border-b-pink-500 data-[state=active]:bg-transparent data-[state=active]:text-pink-500 data-[state=active]:shadow-none",
                "dark:data-[state=active]:text-pink-400",
                "focus-visible:ring-0 focus-visible:ring-offset-0",
              )}
            >
              <Clock className="mr-2 h-4 w-4" />
              Recent
            </TabsTrigger>
          </TabsList>

          <TabsContent value="sets" className="mt-0">
            {isContentLoading ? (
              <div className="py-10 text-center text-muted-foreground">
                Downloading flashcard sets...
              </div>
            ) : sets.length > 0 ? (
              <div className="grid gap-4 md:grid-cols-2">
                {sets.map((set) => (
                  <StudySetCard
                    key={set.id}
                    set={set}
                    onEditClick={handleEditSetClick}
                    onDeleteClick={(id) => {
                      setSetToDelete(id);
                      setIsDeleteDialogOpen(true);
                    }}
                    onAddToFolderClick={handleAddToFolderClick}
                    isDeleteDialogOpen={
                      isDeleteDialogOpen && setToDelete === set.id
                    }
                  />
                ))}
              </div>
            ) : (
              <div className="py-10 text-center text-muted-foreground">
                You don&apos;t have any flashcard sets yet.
              </div>
            )}
          </TabsContent>

          <TabsContent value="folders" className="mt-0">
            {isContentLoading ? (
              <div className="py-10 text-center text-muted-foreground">
                Downloading folders...
              </div>
            ) : (
              <div className="grid gap-4 md:grid-cols-3">
                {folders.map((folder) => (
                  <FolderCard
                    key={folder.id}
                    folder={folder}
                    onEditClick={handleFolderEditClick}
                    onDeleteClick={handleFolderDeleteClick}
                    isMenuForcedOpen={
                      (isFolderDeleteDialogOpen &&
                        folderToDelete?.id === folder.id) ||
                      (isEditFolderFormOpen && folderToEdit?.id === folder.id)
                    }
                  />
                ))}

                <Link href="/profile/folders/create" className="block">
                  <Card className="flex cursor-pointer items-center justify-center border-2 border-dashed border-border bg-card p-6 text-card-foreground transition-colors hover:border-pink-300 hover:bg-pink-50 dark:hover:border-pink-900 dark:hover:bg-pink-950/30">
                    <div className="text-center">
                      <Plus className="mx-auto mb-2 h-8 w-8 text-muted-foreground" />

                      <p className="text-sm font-medium text-muted-foreground">
                        Create new folder
                      </p>
                    </div>
                  </Card>
                </Link>

                {folders.length === 0 && (
                  <div className="col-span-full py-6 text-center text-muted-foreground">
                    You don&apos;t have any folders yet.
                  </div>
                )}
              </div>
            )}
          </TabsContent>

          <TabsContent value="recent" className="mt-0">
            {sets.length > 0 ? (
              <div className="space-y-3">
                {sets.map((set) => (
                  <RecentActivityItem key={set.id} set={set} />
                ))}
              </div>
            ) : (
              <div className="py-10 text-center text-muted-foreground">
                No recent activity yet.
              </div>
            )}
          </TabsContent>
        </Tabs>
      </div>

      <AlertDialog
        open={isDeleteDialogOpen}
        onOpenChange={setIsDeleteDialogOpen}
      >
        <AlertDialogContent className="border-border bg-background text-foreground">
          <AlertDialogHeader>
            <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>

            <AlertDialogDescription>
              This action cannot be undone. This will permanently delete your
              flashcard set and remove all its terms from our servers.
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
              {isDeleting ? "Deleting..." : "Continue"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <AlertDialog
        open={isFolderDeleteDialogOpen}
        onOpenChange={setIsFolderDeleteDialogOpen}
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
              disabled={isDeletingFolder}
              onClick={handleFolderDeleteCancel}
            >
              Cancel
            </AlertDialogCancel>

            <AlertDialogAction
              onClick={handleFolderDeleteConfirm}
              disabled={isDeletingFolder}
              className="bg-red-500 text-white hover:bg-red-600"
            >
              {isDeletingFolder ? "Deleting..." : "Delete"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <EditFolderForm
        folder={folderToEdit}
        isOpen={isEditFolderFormOpen}
        onClose={handleFolderEditClose}
        onFolderUpdated={handleFolderUpdated}
      />

      {setToAddToFolder && (
        <FolderListComponent
          flashcardSetId={setToAddToFolder.id}
          flashcardSetTitle={setToAddToFolder.title}
          isOpen={isFolderListOpen}
          onClose={handleFolderListClose}
          onFolderUpdated={handleFolderUpdated}
        />
      )}
    </>
  );
}
