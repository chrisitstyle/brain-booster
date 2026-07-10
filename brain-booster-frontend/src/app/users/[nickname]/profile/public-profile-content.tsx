"use client";

import { useEffect, useMemo, useState, type ElementType } from "react";
import Link from "next/link";
import {
  BookOpen,
  Calendar,
  FolderOpen,
  Loader2,
  Search,
  User,
} from "lucide-react";
import { toast } from "sonner";

import {
  getFoldersByNickname,
  type Folder as ApiFolder,
} from "@/api/folderService";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

interface FlashcardSet {
  setId: number;
  user: {
    nickname: string;
    createdAt: Date | string;
  };
  setName: string;
  description: string;
  createdAt: Date | string;
  termCount: number;
}

interface PublicProfileContentProps {
  nickname: string;
  initialSets: FlashcardSet[];
}

type ProfileTab = "sets" | "folders";

function formatJoinedDate(createdAt?: Date | string) {
  if (!createdAt) {
    return "Unknown";
  }

  const date = new Date(createdAt);

  if (Number.isNaN(date.getTime())) {
    return "Unknown";
  }

  return new Intl.DateTimeFormat("en-US", {
    month: "long",
    year: "numeric",
  }).format(date);
}

export default function PublicProfileContent({
  nickname,
  initialSets,
}: PublicProfileContentProps) {
  const [activeTab, setActiveTab] = useState<ProfileTab>("sets");

  const [searchQuery, setSearchQuery] = useState("");
  const [folders, setFolders] = useState<ApiFolder[]>([]);
  const [isFoldersLoading, setIsFoldersLoading] = useState(true);

  useEffect(() => {
    let isCancelled = false;

    async function fetchFolders() {
      try {
        setIsFoldersLoading(true);

        const data = await getFoldersByNickname(nickname);

        if (isCancelled) {
          return;
        }

        setFolders(data);
      } catch (error: unknown) {
        if (isCancelled) {
          return;
        }

        console.error("Error fetching folders:", error);

        toast.error("Failed to load folders.");
        setFolders([]);
      } finally {
        if (!isCancelled) {
          setIsFoldersLoading(false);
        }
      }
    }

    void fetchFolders();

    return () => {
      isCancelled = true;
    };
  }, [nickname]);

  const normalizedSearchQuery = searchQuery.trim().toLowerCase();

  const filteredSets = useMemo(() => {
    if (!normalizedSearchQuery) {
      return initialSets;
    }

    return initialSets.filter(
      (set) =>
        set.setName?.toLowerCase().includes(normalizedSearchQuery) ||
        set.description?.toLowerCase().includes(normalizedSearchQuery),
    );
  }, [initialSets, normalizedSearchQuery]);

  const filteredFolders = useMemo(() => {
    if (!normalizedSearchQuery) {
      return folders;
    }

    return folders.filter(
      (folder) =>
        folder.name.toLowerCase().includes(normalizedSearchQuery) ||
        folder.description?.toLowerCase().includes(normalizedSearchQuery),
    );
  }, [folders, normalizedSearchQuery]);

  const displayedNickname = initialSets[0]?.user?.nickname || nickname;

  const joinedDateToDisplay = formatJoinedDate(initialSets[0]?.user?.createdAt);

  const avatarFallback =
    displayedNickname
      .trim()
      .split(/\s+/)
      .map((part) => part.charAt(0))
      .join("")
      .slice(0, 2)
      .toUpperCase() || "?";

  function handleTabChange(value: string) {
    if (value !== "sets" && value !== "folders") {
      return;
    }

    setActiveTab(value);
    setSearchQuery("");
  }

  return (
    <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
      <div className="container mx-auto px-4 py-8">
        <header className="mb-8 flex flex-col items-center text-center">
          <Avatar className="h-24 w-24 border-4 border-pink-200 dark:border-pink-900">
            <AvatarFallback className="bg-pink-100 text-3xl font-semibold text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
              {avatarFallback}
            </AvatarFallback>
          </Avatar>

          <h1 className="mt-4 break-words text-2xl font-bold text-foreground">
            {displayedNickname}
          </h1>

          <p className="break-all text-muted-foreground">
            @{displayedNickname}
          </p>

          <div className="mt-2 flex items-center gap-2 text-sm text-muted-foreground">
            <Calendar className="h-4 w-4" aria-hidden="true" />

            <span>Joined {joinedDateToDisplay}</span>
          </div>
        </header>

        <Tabs
          value={activeTab}
          onValueChange={handleTabChange}
          className="w-full"
        >
          <TabsList className="mb-6 h-auto w-full justify-center gap-2 rounded-none border-b border-border bg-transparent p-0">
            <TabsTrigger
              value="sets"
              className="rounded-none border-0 border-b-2 border-transparent bg-transparent px-6 py-3 text-muted-foreground shadow-none transition-colors hover:text-pink-500 focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 data-[state=active]:border-pink-500 data-[state=active]:bg-transparent data-[state=active]:text-pink-500 data-[state=active]:shadow-none dark:hover:text-pink-400 dark:data-[state=active]:border-pink-400 dark:data-[state=active]:text-pink-400"
            >
              <BookOpen className="mr-2 h-4 w-4" aria-hidden="true" />
              Sets ({initialSets.length})
            </TabsTrigger>

            <TabsTrigger
              value="folders"
              className="rounded-none border-0 border-b-2 border-transparent bg-transparent px-6 py-3 text-muted-foreground shadow-none transition-colors hover:text-pink-500 focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 data-[state=active]:border-pink-500 data-[state=active]:bg-transparent data-[state=active]:text-pink-500 data-[state=active]:shadow-none dark:hover:text-pink-400 dark:data-[state=active]:border-pink-400 dark:data-[state=active]:text-pink-400"
            >
              <FolderOpen className="mr-2 h-4 w-4" aria-hidden="true" />
              Folders ({folders.length})
            </TabsTrigger>
          </TabsList>

          <TabsContent value="sets" className="mt-0">
            <ProfileSearchInput
              value={searchQuery}
              placeholder="Search sets..."
              ariaLabel="Search flashcard sets"
              onChange={setSearchQuery}
            />

            {filteredSets.length > 0 ? (
              <div className="mx-auto max-w-2xl space-y-4">
                {filteredSets.map((set) => (
                  <PublicStudySetCard
                    key={set.setId}
                    set={set}
                    nickname={displayedNickname}
                  />
                ))}
              </div>
            ) : (
              <EmptyState
                icon={BookOpen}
                title="No flashcard sets found"
                description={
                  normalizedSearchQuery
                    ? "Try a different search term."
                    : "This user hasn't created any flashcard sets yet."
                }
              />
            )}
          </TabsContent>

          <TabsContent value="folders" className="mt-0">
            <ProfileSearchInput
              value={searchQuery}
              placeholder="Search folders..."
              ariaLabel="Search folders"
              onChange={setSearchQuery}
            />

            {isFoldersLoading ? (
              <div
                className="flex items-center justify-center gap-3 py-12 text-muted-foreground"
                role="status"
              >
                <Loader2
                  className="h-5 w-5 animate-spin text-pink-500 dark:text-pink-400"
                  aria-hidden="true"
                />

                <span>Downloading folders...</span>
              </div>
            ) : filteredFolders.length > 0 ? (
              <div className="mx-auto max-w-2xl space-y-4">
                {filteredFolders.map((folder) => (
                  <PublicFolderCard
                    key={folder.folderId}
                    folder={folder}
                    nickname={displayedNickname}
                  />
                ))}
              </div>
            ) : (
              <EmptyState
                icon={FolderOpen}
                title="No folders found"
                description={
                  normalizedSearchQuery
                    ? "Try a different search term."
                    : "This user hasn't created any folders yet."
                }
              />
            )}
          </TabsContent>
        </Tabs>
      </div>
    </main>
  );
}

interface ProfileSearchInputProps {
  value: string;
  placeholder: string;
  ariaLabel: string;
  onChange: (value: string) => void;
}

function ProfileSearchInput({
  value,
  placeholder,
  ariaLabel,
  onChange,
}: ProfileSearchInputProps) {
  return (
    <div className="mb-6">
      <div className="relative mx-auto max-w-2xl">
        <Search
          className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
          aria-hidden="true"
        />

        <Input
          type="search"
          placeholder={placeholder}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          className="border-input bg-background pl-10 text-foreground placeholder:text-muted-foreground focus-visible:border-pink-300 focus-visible:ring-pink-500/20 dark:focus-visible:border-pink-800"
          aria-label={ariaLabel}
        />
      </div>
    </div>
  );
}

function PublicStudySetCard({
  set,
  nickname,
}: {
  set: FlashcardSet;
  nickname: string;
}) {
  const encodedNickname = encodeURIComponent(nickname);

  const setHref = `/users/${encodedNickname}/sets/${set.setId}`;

  const profileHref = `/users/${encodedNickname}/profile`;

  return (
    <Card className="group border-border bg-card text-card-foreground transition-all hover:border-pink-200 hover:shadow-md dark:hover:border-pink-900">
      <CardContent className="p-4">
        <div className="mb-3">
          <Link
            href={setHref}
            className="break-words font-semibold text-card-foreground transition-colors hover:text-pink-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring dark:hover:text-pink-400"
          >
            {set.setName}
          </Link>

          {set.description && (
            <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
              {set.description}
            </p>
          )}

          <p className="mt-1 text-sm text-muted-foreground">
            {set.termCount} {set.termCount === 1 ? "term" : "terms"}
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Avatar className="h-6 w-6">
            <AvatarFallback className="bg-pink-100 text-xs text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
              <User className="h-3 w-3" aria-hidden="true" />
            </AvatarFallback>
          </Avatar>

          <Link
            href={profileHref}
            className="truncate text-sm text-muted-foreground transition-colors hover:text-pink-500 dark:hover:text-pink-400"
          >
            {nickname}
          </Link>
        </div>
      </CardContent>
    </Card>
  );
}

function PublicFolderCard({
  folder,
  nickname,
}: {
  folder: ApiFolder;
  nickname: string;
}) {
  const encodedNickname = encodeURIComponent(nickname);

  const profileHref = `/users/${encodedNickname}/profile`;

  return (
    <Card className="group border-border bg-card text-card-foreground transition-all hover:border-pink-200 hover:shadow-md dark:hover:border-pink-900">
      <CardContent className="p-4">
        <div className="mb-3 flex items-center gap-3">
          <div className="shrink-0 rounded-lg bg-pink-100 p-2 dark:bg-pink-950/50">
            <FolderOpen
              className="h-5 w-5 text-pink-500 dark:text-pink-400"
              aria-hidden="true"
            />
          </div>

          <div className="min-w-0">
            <Link
              href={`/profile/folders/${folder.folderId}`}
              className="break-words font-semibold text-card-foreground transition-colors hover:text-pink-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring dark:hover:text-pink-400"
            >
              {folder.name}
            </Link>

            <p className="text-sm text-muted-foreground">
              {folder.setCount} {folder.setCount === 1 ? "set" : "sets"}
            </p>
          </div>
        </div>

        {folder.description && (
          <p className="mb-3 line-clamp-2 text-sm text-muted-foreground">
            {folder.description}
          </p>
        )}

        <div className="flex items-center gap-2">
          <Avatar className="h-6 w-6">
            <AvatarFallback className="bg-pink-100 text-xs text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
              <User className="h-3 w-3" aria-hidden="true" />
            </AvatarFallback>
          </Avatar>

          <Link
            href={profileHref}
            className="truncate text-sm text-muted-foreground transition-colors hover:text-pink-500 dark:hover:text-pink-400"
          >
            {nickname}
          </Link>
        </div>
      </CardContent>
    </Card>
  );
}

interface EmptyStateProps {
  icon: ElementType;
  title: string;
  description: string;
}

function EmptyState({ icon: Icon, title, description }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      <div className="mb-4 rounded-full bg-muted p-4">
        <Icon className="h-8 w-8 text-muted-foreground" aria-hidden="true" />
      </div>

      <h3 className="text-lg font-medium text-foreground">{title}</h3>

      <p className="mt-1 text-sm text-muted-foreground">{description}</p>
    </div>
  );
}
