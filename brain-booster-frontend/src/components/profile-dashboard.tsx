"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  BookOpen,
  Clock,
  Flame,
  FolderOpen,
  MoreHorizontal,
  Plus,
  Settings,
  Trophy,
  Zap,
} from "lucide-react";
import { toast } from "sonner";

import { deleteFlashcardSet } from "@/api/flashcardSetService";
import {
  deleteFolderById,
  getMyFolders,
  type Folder as FolderDTO,
} from "@/api/folderService";
import { getCurrentUser, type UserDTO } from "@/api/profileService";
import { getUserFlashcardSetsByUserId } from "@/api/userService";
import EditFolderForm from "@/app/profile/folders/components/edit-folder-form";
import FolderListComponent from "@/app/profile/folders/components/folder-list-component";
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
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Progress } from "@/components/ui/progress";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useAuth } from "@/context/AuthContext";
import { cn } from "@/lib/utils";
import { PROFILE_UPDATED_EVENT } from "@/utils/profile-events";

interface StudySet {
  id: string;
  title: string;
  description: string;
  termCount: number;
  author: string;
  lastStudied?: string;
}

interface FlashcardSetDTO {
  setId: number;
  user: {
    nickname: string;
  };
  setName: string;
  description: string;
  createdAt: string;
  termCount: number;
}

interface DashboardFolder {
  id: string;
  title: string;
  description: string;
  setCount: number;
}

interface ProfileDashboardData {
  user: UserDTO;
  sets: StudySet[];
  folders: DashboardFolder[];
}

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

async function getProfileDashboardData(
  token: string,
): Promise<ProfileDashboardData> {
  const user = await getCurrentUser(token);

  const [setsData, foldersData] = await Promise.all([
    getUserFlashcardSetsByUserId(user.userId, token),
    getMyFolders(token),
  ]);

  const formattedSets: StudySet[] = setsData.map((set: FlashcardSetDTO) => ({
    id: set.setId.toString(),
    title: set.setName,
    description: set.description,
    termCount: set.termCount,
    author: set.user.nickname || user.nickname,
    lastStudied: new Date(set.createdAt).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
    }),
  }));

  const formattedFolders: DashboardFolder[] = foldersData.map(
    (folder: FolderDTO) => ({
      id: folder.folderId.toString(),
      title: folder.name,
      description: folder.description ?? "",
      setCount: folder.setCount,
    }),
  );

  return {
    user,
    sets: formattedSets,
    folders: formattedFolders,
  };
}

export function ProfileDashboard() {
  const router = useRouter();
  const { token } = useAuth();

  const [activeTab, setActiveTab] = useState("sets");

  const [currentUser, setCurrentUser] = useState<UserDTO | null>(null);

  const [sets, setSets] = useState<StudySet[]>([]);
  const [folders, setFolders] = useState<DashboardFolder[]>([]);

  const [loadedContentToken, setLoadedContentToken] = useState<string | null>(
    null,
  );

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

  const isContentLoading = Boolean(token && loadedContentToken !== token);

  useEffect(() => {
    if (!token) {
      return;
    }

    let isCancelled = false;
    const requestToken = token;

    void getProfileDashboardData(requestToken)
      .then(({ user, sets: loadedSets, folders: loadedFolders }) => {
        if (isCancelled) {
          return;
        }

        setCurrentUser(user);
        setSets(loadedSets);
        setFolders(loadedFolders);
      })
      .catch((error: unknown) => {
        if (isCancelled) {
          return;
        }

        console.error("Error fetching profile content:", error);

        setCurrentUser(null);
        setSets([]);
        setFolders([]);

        toast.error("Failed to load profile content.");
      })
      .finally(() => {
        if (!isCancelled) {
          setLoadedContentToken(requestToken);
        }
      });

    return () => {
      isCancelled = true;
    };
  }, [token]);

  useEffect(() => {
    if (!token) {
      return;
    }

    let isCancelled = false;
    const requestToken = token;

    function handleProfileUpdated() {
      void getProfileDashboardData(requestToken)
        .then(({ user, sets: loadedSets, folders: loadedFolders }) => {
          if (isCancelled) {
            return;
          }

          setCurrentUser(user);
          setSets(loadedSets);
          setFolders(loadedFolders);
        })
        .catch((error: unknown) => {
          if (isCancelled) {
            return;
          }

          console.error("Error refreshing profile content:", error);

          toast.error("Failed to refresh profile content.");
        });
    }

    window.addEventListener(PROFILE_UPDATED_EVENT, handleProfileUpdated);

    return () => {
      isCancelled = true;

      window.removeEventListener(PROFILE_UPDATED_EVENT, handleProfileUpdated);
    };
  }, [token]);

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

function StudySetCard({
  set,
  onEditClick,
  onDeleteClick,
  onAddToFolderClick,
  isDeleteDialogOpen,
}: {
  set: StudySet;
  onEditClick: (set: StudySet) => void;
  onDeleteClick: (id: string) => void;
  onAddToFolderClick: (set: StudySet) => void;
  isDeleteDialogOpen: boolean;
}) {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  const authorInitial = set.author.trim().charAt(0).toUpperCase() || "?";

  return (
    <Card className="group border-border bg-card text-card-foreground transition-all hover:border-pink-200 hover:shadow-md dark:hover:border-pink-900">
      <CardContent className="p-4">
        <div className="mb-3 flex items-start justify-between gap-3">
          <div className="min-w-0 flex-1">
            <Link
              href={`/users/${encodeURIComponent(set.author)}/sets/${set.id}`}
              className="line-clamp-1 font-semibold text-card-foreground transition-colors hover:text-pink-500 dark:hover:text-pink-400"
            >
              {set.title}
            </Link>

            {set.description && (
              <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
                {set.description}
              </p>
            )}

            <p className="mt-2 text-xs font-medium uppercase tracking-wide text-muted-foreground">
              {set.termCount} terms
            </p>
          </div>

          <DropdownMenu open={isDropdownOpen} onOpenChange={setIsDropdownOpen}>
            <DropdownMenuTrigger asChild>
              <Button
                type="button"
                variant="ghost"
                size="icon"
                className={cn(
                  "h-8 w-8 shrink-0 transition-opacity",
                  isDropdownOpen || isDeleteDialogOpen
                    ? "opacity-100"
                    : "opacity-0 group-hover:opacity-100 group-focus-within:opacity-100",
                )}
                aria-label={`Open options for ${set.title}`}
              >
                <MoreHorizontal className="h-4 w-4 text-muted-foreground" />
              </Button>
            </DropdownMenuTrigger>

            <DropdownMenuContent
              align="end"
              className="border-border bg-popover text-popover-foreground"
            >
              <DropdownMenuItem
                className="cursor-pointer"
                onSelect={(event) => {
                  event.preventDefault();
                  onEditClick(set);
                  setIsDropdownOpen(false);
                }}
              >
                Edit
              </DropdownMenuItem>

              <DropdownMenuItem>Share</DropdownMenuItem>

              <DropdownMenuItem
                className="cursor-pointer"
                onSelect={(event) => {
                  event.preventDefault();
                  onAddToFolderClick(set);
                  setIsDropdownOpen(false);
                }}
              >
                Add to folder
              </DropdownMenuItem>

              <DropdownMenuItem
                className="cursor-pointer text-red-500 focus:bg-red-50 focus:text-red-600 dark:focus:bg-red-950/40 dark:focus:text-red-400"
                onSelect={(event) => {
                  event.preventDefault();
                  onDeleteClick(set.id);
                  setIsDropdownOpen(false);
                }}
              >
                Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>

        <div className="flex items-center gap-2">
          <Avatar className="h-6 w-6">
            <AvatarFallback className="bg-pink-100 text-xs text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
              {authorInitial}
            </AvatarFallback>
          </Avatar>

          <span className="truncate text-sm text-muted-foreground">
            {set.author}
          </span>
        </div>
      </CardContent>
    </Card>
  );
}

function FolderCard({
  folder,
  onEditClick,
  onDeleteClick,
  isMenuForcedOpen,
}: {
  folder: DashboardFolder;
  onEditClick: (folder: DashboardFolder) => void;
  onDeleteClick: (folder: DashboardFolder) => void;
  isMenuForcedOpen: boolean;
}) {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  return (
    <Card className="group border-border bg-card text-card-foreground transition-all hover:border-pink-200 hover:shadow-md dark:hover:border-pink-900">
      <CardContent className="p-4">
        <div className="flex items-start justify-between gap-3">
          <Link
            href={`/profile/folders/${folder.id}`}
            className="flex min-w-0 flex-1 items-center gap-3"
          >
            <div className="rounded-lg bg-pink-100 p-2 dark:bg-pink-950/50">
              <FolderOpen className="h-5 w-5 text-pink-500 dark:text-pink-400" />
            </div>

            <div className="min-w-0">
              <h3 className="line-clamp-1 font-semibold text-card-foreground transition-colors group-hover:text-pink-500 dark:group-hover:text-pink-400">
                {folder.title}
              </h3>

              <p className="text-sm text-muted-foreground">
                {folder.setCount} {folder.setCount === 1 ? "set" : "sets"}
              </p>
            </div>
          </Link>

          <DropdownMenu open={isDropdownOpen} onOpenChange={setIsDropdownOpen}>
            <DropdownMenuTrigger asChild>
              <Button
                type="button"
                variant="ghost"
                size="icon"
                className={cn(
                  "h-8 w-8 shrink-0 transition-opacity",
                  isDropdownOpen || isMenuForcedOpen
                    ? "opacity-100"
                    : "opacity-0 group-hover:opacity-100 group-focus-within:opacity-100",
                )}
                aria-label={`Open options for ${folder.title}`}
              >
                <MoreHorizontal className="h-4 w-4 text-muted-foreground" />
              </Button>
            </DropdownMenuTrigger>

            <DropdownMenuContent
              align="end"
              className="border-border bg-popover text-popover-foreground"
            >
              <DropdownMenuItem
                className="cursor-pointer"
                onSelect={(event) => {
                  event.preventDefault();
                  onEditClick(folder);
                  setIsDropdownOpen(false);
                }}
              >
                Edit
              </DropdownMenuItem>

              <DropdownMenuItem
                className="cursor-pointer text-red-500 focus:bg-red-50 focus:text-red-600 dark:focus:bg-red-950/40 dark:focus:text-red-400"
                onSelect={(event) => {
                  event.preventDefault();
                  onDeleteClick(folder);
                  setIsDropdownOpen(false);
                }}
              >
                Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </CardContent>
    </Card>
  );
}

function RecentActivityItem({ set }: { set: StudySet }) {
  return (
    <div className="flex flex-col gap-4 rounded-lg border border-border bg-card p-4 text-card-foreground transition-all hover:border-pink-200 hover:shadow-sm dark:hover:border-pink-900 sm:flex-row sm:items-center sm:justify-between">
      <div className="flex min-w-0 items-center gap-4">
        <div className="shrink-0 rounded-lg bg-pink-100 p-2 dark:bg-pink-950/50">
          <BookOpen className="h-5 w-5 text-pink-500 dark:text-pink-400" />
        </div>

        <div className="min-w-0">
          <Link
            href={`/users/${encodeURIComponent(set.author)}/sets/${set.id}`}
            className="line-clamp-1 font-medium text-card-foreground transition-colors hover:text-pink-500 dark:hover:text-pink-400"
          >
            {set.title}
          </Link>

          <p className="text-sm text-muted-foreground">{set.termCount} terms</p>
        </div>
      </div>

      <div className="flex items-center justify-between gap-4 sm:justify-end">
        <span className="text-sm text-muted-foreground">{set.lastStudied}</span>

        <Button
          size="sm"
          className="bg-pink-500 text-white hover:bg-pink-600"
          asChild
        >
          <Link
            href={`/users/${encodeURIComponent(set.author)}/sets/${set.id}`}
          >
            Study
          </Link>
        </Button>
      </div>
    </div>
  );
}
