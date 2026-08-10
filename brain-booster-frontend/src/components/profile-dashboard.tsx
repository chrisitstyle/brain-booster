"use client";

import { BookOpen, Clock, FolderOpen, Plus } from "lucide-react";
import Link from "next/link";
import { type ReactNode, useState } from "react";

import { AchievementsSection } from "@/app/profile/components/achievements-section";
import { FolderCard } from "@/app/profile/components/folder-card";
import { ProfileHeader } from "@/app/profile/components/profile-header";
import { RecentActivityItem } from "@/app/profile/components/recent-activity-item";
import { StudySetCard } from "@/app/profile/components/study-set-card";
import { WeeklyProgressCard } from "@/app/profile/components/weekly-progress-card";
import EditFolderForm from "@/app/profile/folders/components/edit-folder-form";
import FolderListComponent from "@/app/profile/folders/components/folder-list-component";
import { useProfileDashboardActions } from "@/app/profile/hooks/use-profile-dashboard-actions";
import { useProfileDashboardData } from "@/app/profile/hooks/use-profile-dashboard-data";
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
import { Card } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useAuth } from "@/context/AuthContext";
import { cn } from "@/lib/utils";

export function ProfileDashboard() {
  const { token } = useAuth();

  const { currentUser, sets, setSets, folders, setFolders, isContentLoading } =
    useProfileDashboardData(token);

  const {
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
  } = useProfileDashboardActions({
    token,
    setSets,
    setFolders,
  });

  const [activeTab, setActiveTab] = useState("sets");

  let studySetsContent: ReactNode;

  if (isContentLoading) {
    studySetsContent = (
      <div className="py-10 text-center text-muted-foreground">
        Downloading flashcard sets...
      </div>
    );
  } else if (sets.length === 0) {
    studySetsContent = (
      <div className="py-10 text-center text-muted-foreground">
        You don&apos;t have any flashcard sets yet.
      </div>
    );
  } else {
    studySetsContent = (
      <div className="grid gap-4 md:grid-cols-2">
        {sets.map((set) => (
          <StudySetCard
            key={set.id}
            set={set}
            onEditClick={handleEditSetClick}
            onDeleteClick={handleDeleteSetClick}
            onAddToFolderClick={handleAddToFolderClick}
            isDeleteDialogOpen={isDeleteDialogOpen && setToDelete === set.id}
          />
        ))}
      </div>
    );
  }

  return (
    <>
      <div className="container mx-auto px-4 py-8">
        <ProfileHeader
          currentUser={currentUser}
          isContentLoading={isContentLoading}
        />

        <AchievementsSection />

        <WeeklyProgressCard />

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
            {studySetsContent}
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
