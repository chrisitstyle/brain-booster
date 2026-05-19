"use client";

import Link from "next/link";
import {
  BookOpen,
  Clock,
  Flame,
  FolderOpen,
  MoreHorizontal,
  Plus,
  Settings,
  Trophy,
  User,
  Zap,
} from "lucide-react";

import { toast } from "sonner";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Progress } from "@/components/ui/progress";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
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

import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { parseJwt } from "@/utils/jwt";
import { getUserFlashcardSetsByUserId } from "@/api/userService";
import { deleteFlashcardSet } from "@/api/flashcardSetService";
import {
  deleteFolderById,
  getMyFolders,
  type Folder as FolderDTO,
} from "@/api/folderService";
import FolderListComponent from "@/app/profile/folders/components/folder-list-component";
import EditFolderForm from "@/app/profile/folders/components/edit-folder-form";

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

const achievements = [
  { icon: Flame, label: "7 Day Streak", value: "7", color: "text-orange-500" },
  {
    icon: Trophy,
    label: "Sets Mastered",
    value: "12",
    color: "text-yellow-500",
  },
  { icon: Zap, label: "Terms Learned", value: "847", color: "text-pink-500" },
];

export function ProfileDashboard() {
  const [activeTab, setActiveTab] = useState("sets");
  const { token } = useAuth();

  const [sets, setSets] = useState<StudySet[]>([]);
  const [folders, setFolders] = useState<DashboardFolder[]>([]);
  const [isLoading, setIsLoading] = useState(true);

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

  useEffect(() => {
    const fetchUserContent = async () => {
      if (!token) {
        setIsLoading(false);
        return;
      }

      const decoded = parseJwt(token);

      if (!decoded?.id) {
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);

        const [setsData, foldersData] = await Promise.all([
          getUserFlashcardSetsByUserId(decoded.id, token),
          getMyFolders(token),
        ]);

        const formattedSets: StudySet[] = setsData.map(
          (set: FlashcardSetDTO) => ({
            id: set.setId.toString(),
            title: set.setName,
            description: set.description,
            termCount: set.termCount,
            author: set.user.nickname || "You",
            lastStudied: new Date(set.createdAt).toLocaleDateString("en-US", {
              month: "short",
              day: "numeric",
            }),
          }),
        );

        const formattedFolders: DashboardFolder[] = foldersData.map(
          (folder: FolderDTO) => ({
            id: folder.folderId.toString(),
            title: folder.name,
            description: folder.description ?? "",
            setCount: folder.setCount,
          }),
        );

        setSets(formattedSets);
        setFolders(formattedFolders);
      } catch (error) {
        console.error("Error fetching profile content:", error);

        toast.error("Failed to load profile content.", {
          style: {
            background: "red",
            color: "white",
          },
        });
      } finally {
        setIsLoading(false);
      }
    };

    fetchUserContent();
  }, [token]);

  const handleAddToFolderClick = (set: StudySet) => {
    setSetToAddToFolder(set);
    setIsFolderListOpen(true);
  };

  const handleFolderListClose = () => {
    setSetToAddToFolder(null);
    setIsFolderListOpen(false);
  };

  const handleFolderUpdated = (updatedFolder: FolderDTO) => {
    setFolders((prevFolders) =>
      prevFolders.map((folder) =>
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
  };

  const handleDeleteConfirm = async () => {
    if (!setToDelete || !token) return;

    try {
      setIsDeleting(true);

      await deleteFlashcardSet(setToDelete, token);

      setSets((prevSets) => prevSets.filter((set) => set.id !== setToDelete));

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

  const handleDeleteCancel = () => {
    setSetToDelete(null);
    setIsDeleteDialogOpen(false);
  };

  const handleFolderEditClick = (folder: DashboardFolder) => {
    setFolderToEdit(folder);
    setIsEditFolderFormOpen(true);
  };

  const handleFolderEditClose = () => {
    setFolderToEdit(null);
    setIsEditFolderFormOpen(false);
  };

  const handleFolderDeleteClick = (folder: DashboardFolder) => {
    setFolderToDelete(folder);
    setIsFolderDeleteDialogOpen(true);
  };

  const handleFolderDeleteCancel = () => {
    if (isDeletingFolder) return;

    setFolderToDelete(null);
    setIsFolderDeleteDialogOpen(false);
  };

  const handleFolderDeleteConfirm = async () => {
    if (!folderToDelete || !token) return;

    try {
      setIsDeletingFolder(true);

      await deleteFolderById(folderToDelete.id, token);

      setFolders((prevFolders) =>
        prevFolders.filter((folder) => folder.id !== folderToDelete.id),
      );

      toast.success("Folder deleted successfully", {
        style: {
          background: "green",
          color: "white",
        },
      });
    } catch (error) {
      console.error("Failed to delete folder:", error);

      toast.error("Failed to delete folder. Please try again.", {
        style: {
          background: "red",
          color: "white",
        },
      });
    } finally {
      setIsDeletingFolder(false);
      setIsFolderDeleteDialogOpen(false);
      setFolderToDelete(null);
    }
  };

  return (
    <>
      <div className="container mx-auto px-4 py-8">
        <div className="mb-8 flex flex-col items-start gap-6 md:flex-row md:items-center md:justify-between">
          <div className="flex items-center gap-4">
            <Avatar className="h-20 w-20 border-4 border-pink-200">
              <AvatarImage
                src="/placeholder.svg?height=80&width=80"
                alt="User avatar"
              />
              <AvatarFallback className="bg-pink-100 text-2xl text-pink-500">
                JD
              </AvatarFallback>
            </Avatar>

            <div>
              <h1 className="text-2xl font-bold text-gray-800">John Doe</h1>
              <p className="text-gray-500">@johndoe</p>
              <p className="mt-1 text-sm text-gray-400">
                Member since January 2024
              </p>
            </div>
          </div>

          <div className="flex gap-3">
            <Button
              variant="outline"
              size="sm"
              className="border-gray-200 text-gray-600 hover:border-pink-200 hover:text-pink-500"
            >
              <Settings className="mr-2 h-4 w-4" />
              Settings
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
          {achievements.map((achievement, index) => (
            <Card key={index} className="border-gray-200 bg-white">
              <CardContent className="flex items-center gap-4 p-6">
                <div
                  className={cn(
                    "rounded-full bg-gray-50 p-3",
                    achievement.color,
                  )}
                >
                  <achievement.icon className="h-6 w-6" />
                </div>

                <div>
                  <p className="text-2xl font-bold text-gray-800">
                    {achievement.value}
                  </p>
                  <p className="text-sm text-gray-500">{achievement.label}</p>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

        <Card className="mb-8 border-gray-200 bg-white">
          <CardHeader className="pb-2">
            <CardTitle className="text-lg font-semibold text-gray-800">
              Weekly Progress
            </CardTitle>
          </CardHeader>

          <CardContent>
            <div className="mb-2 flex items-center justify-between">
              <span className="text-sm text-gray-500">5 of 7 days studied</span>
              <span className="text-sm font-medium text-pink-500">71%</span>
            </div>

            <Progress value={71} className="h-2 bg-gray-100" />

            <div className="mt-4 flex justify-between">
              {["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"].map(
                (day, index) => (
                  <div key={day} className="flex flex-col items-center gap-2">
                    <div
                      className={cn(
                        "flex h-8 w-8 items-center justify-center rounded-full text-xs font-medium",
                        index < 5
                          ? "bg-pink-500 text-white"
                          : "bg-gray-100 text-gray-400",
                      )}
                    >
                      {index < 5 ? <Flame className="h-4 w-4" /> : null}
                    </div>
                    <span className="text-xs text-gray-500">{day}</span>
                  </div>
                ),
              )}
            </div>
          </CardContent>
        </Card>

        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
          <TabsList className="mb-6 w-full justify-start gap-2 border-b border-gray-200 bg-transparent p-0">
            <TabsTrigger
              value="sets"
              className={cn(
                "rounded-none border-0 border-b-2 border-transparent bg-transparent px-4 py-3 text-gray-600 shadow-none",
                "data-[state=active]:border-b-pink-500 data-[state=active]:bg-transparent data-[state=active]:text-pink-500 data-[state=active]:shadow-none",
                "focus-visible:ring-0 focus-visible:ring-offset-0",
              )}
            >
              <BookOpen className="mr-2 h-4 w-4" />
              Study Sets
            </TabsTrigger>

            <TabsTrigger
              value="folders"
              className={cn(
                "rounded-none border-0 border-b-2 border-transparent bg-transparent px-4 py-3 text-gray-600 shadow-none",
                "data-[state=active]:border-b-pink-500 data-[state=active]:bg-transparent data-[state=active]:text-pink-500 data-[state=active]:shadow-none",
                "focus-visible:ring-0 focus-visible:ring-offset-0",
              )}
            >
              <FolderOpen className="mr-2 h-4 w-4" />
              Folders
            </TabsTrigger>

            <TabsTrigger
              value="recent"
              className={cn(
                "rounded-none border-0 border-b-2 border-transparent bg-transparent px-4 py-3 text-gray-600 shadow-none",
                "data-[state=active]:border-b-pink-500 data-[state=active]:bg-transparent data-[state=active]:text-pink-500 data-[state=active]:shadow-none",
                "focus-visible:ring-0 focus-visible:ring-offset-0",
              )}
            >
              <Clock className="mr-2 h-4 w-4" />
              Recent
            </TabsTrigger>
          </TabsList>

          <TabsContent value="sets" className="mt-0">
            {isLoading ? (
              <div className="py-10 text-center text-gray-500">
                Downloading flashcard sets...
              </div>
            ) : sets.length > 0 ? (
              <div className="grid gap-4 md:grid-cols-2">
                {sets.map((set) => (
                  <StudySetCard
                    key={set.id}
                    set={set}
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
              <div className="py-10 text-center text-gray-500">
                You don&apos;t have any flashcard sets yet.
              </div>
            )}
          </TabsContent>

          <TabsContent value="folders" className="mt-0">
            {isLoading ? (
              <div className="py-10 text-center text-gray-500">
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
                  <Card className="flex cursor-pointer items-center justify-center border-2 border-dashed border-gray-200 bg-white p-6 transition-colors hover:border-pink-300 hover:bg-pink-50">
                    <div className="text-center">
                      <Plus className="mx-auto mb-2 h-8 w-8 text-gray-400" />
                      <p className="text-sm font-medium text-gray-500">
                        Create new folder
                      </p>
                    </div>
                  </Card>
                </Link>

                {folders.length === 0 && (
                  <div className="col-span-full py-6 text-center text-gray-500">
                    You don&apos;t have any folders yet.
                  </div>
                )}
              </div>
            )}
          </TabsContent>

          <TabsContent value="recent" className="mt-0">
            <div className="space-y-3">
              {sets.map((set) => (
                <RecentActivityItem key={set.id} set={set} />
              ))}
            </div>
          </TabsContent>
        </Tabs>
      </div>

      <AlertDialog
        open={isDeleteDialogOpen}
        onOpenChange={setIsDeleteDialogOpen}
      >
        <AlertDialogContent>
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
        <AlertDialogContent>
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
  onDeleteClick,
  onAddToFolderClick,
  isDeleteDialogOpen,
}: {
  set: StudySet;
  onDeleteClick: (id: string) => void;
  onAddToFolderClick: (set: StudySet) => void;
  isDeleteDialogOpen: boolean;
}) {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  return (
    <Card className="group border-gray-200 bg-white transition-all hover:border-pink-200 hover:shadow-md">
      <CardContent className="p-4">
        <div className="mb-3 flex items-start justify-between">
          <div className="flex-1">
            <Link
              href={`/users/${set.author}/sets/${set.id}`}
              className="line-clamp-1 font-semibold text-gray-800 hover:text-pink-500"
            >
              {set.title}
            </Link>

            {set.description && (
              <p className="mt-1 line-clamp-2 text-sm text-gray-600">
                {set.description}
              </p>
            )}

            <p className="mt-2 text-xs font-medium uppercase tracking-wide text-gray-400">
              {set.termCount} terms
            </p>
          </div>

          <DropdownMenu open={isDropdownOpen} onOpenChange={setIsDropdownOpen}>
            <DropdownMenuTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                className={cn(
                  "h-8 w-8 transition-opacity",
                  isDropdownOpen || isDeleteDialogOpen
                    ? "opacity-100"
                    : "opacity-0 group-hover:opacity-100",
                )}
              >
                <MoreHorizontal className="h-4 w-4 text-gray-400" />
              </Button>
            </DropdownMenuTrigger>

            <DropdownMenuContent align="end">
              <DropdownMenuItem>Edit</DropdownMenuItem>
              <DropdownMenuItem>Share</DropdownMenuItem>

              <DropdownMenuItem
                onSelect={(event) => {
                  event.preventDefault();
                  onAddToFolderClick(set);
                  setIsDropdownOpen(false);
                }}
              >
                Add to folder
              </DropdownMenuItem>

              <DropdownMenuItem
                className="cursor-pointer text-red-500"
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
            <AvatarFallback className="bg-pink-100 text-xs text-pink-500">
              <User className="h-3 w-3" />
            </AvatarFallback>
          </Avatar>

          <span className="text-sm text-gray-500">{set.author}</span>
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
    <Card className="group border-gray-200 bg-white transition-all hover:border-pink-200 hover:shadow-md">
      <CardContent className="p-4">
        <div className="flex items-start justify-between gap-3">
          <Link
            href={`/profile/folders/${folder.id}`}
            className="flex min-w-0 flex-1 items-center gap-3"
          >
            <div className="rounded-lg bg-pink-100 p-2">
              <FolderOpen className="h-5 w-5 text-pink-500" />
            </div>

            <div className="min-w-0">
              <h3 className="line-clamp-1 font-semibold text-gray-800 transition-colors group-hover:text-pink-500">
                {folder.title}
              </h3>

              <p className="text-sm text-gray-500">
                {folder.setCount} {folder.setCount === 1 ? "set" : "sets"}
              </p>
            </div>
          </Link>

          <DropdownMenu open={isDropdownOpen} onOpenChange={setIsDropdownOpen}>
            <DropdownMenuTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                className={cn(
                  "h-8 w-8 transition-opacity",
                  isDropdownOpen || isMenuForcedOpen
                    ? "opacity-100"
                    : "opacity-0 group-hover:opacity-100",
                )}
              >
                <MoreHorizontal className="h-4 w-4 text-gray-400" />
              </Button>
            </DropdownMenuTrigger>

            <DropdownMenuContent align="end">
              <DropdownMenuItem
                onSelect={(event) => {
                  event.preventDefault();
                  onEditClick(folder);
                  setIsDropdownOpen(false);
                }}
              >
                Edit
              </DropdownMenuItem>

              <DropdownMenuItem
                className="cursor-pointer text-red-500"
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
    <div className="flex items-center justify-between rounded-lg border border-gray-200 bg-white p-4 transition-all hover:border-pink-200 hover:shadow-sm">
      <div className="flex items-center gap-4">
        <div className="rounded-lg bg-pink-100 p-2">
          <BookOpen className="h-5 w-5 text-pink-500" />
        </div>

        <div>
          <Link
            href={`/users/${set.author}/sets/${set.id}`}
            className="font-medium text-gray-800 hover:text-pink-500"
          >
            {set.title}
          </Link>

          <p className="text-sm text-gray-500">{set.termCount} terms</p>
        </div>
      </div>

      <div className="flex items-center gap-4">
        <span className="text-sm text-gray-400">{set.lastStudied}</span>

        <Button
          size="sm"
          className="bg-pink-500 text-white hover:bg-pink-600"
          asChild
        >
          <Link href={`/users/${set.author}/sets/${set.id}`}>Study</Link>
        </Button>
      </div>
    </div>
  );
}
