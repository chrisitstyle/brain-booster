"use client";

import Link from "next/link";
import { BookOpen, Calendar, FolderOpen, Search, User } from "lucide-react";

import { cn } from "@/lib/utils";
import { Input } from "@/components/ui/input";
import { Card, CardContent } from "@/components/ui/card";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useState } from "react";

interface FlashcardSet {
  setId: number;
  user: { nickname: string; createdAt: Date };
  setName: string;
  description: string;
  createdAt: Date;
  termCount: number;
}

interface Folder {
  folderId: string;
  title: string;
  setCount: number;
}

// Mock data for johndoe example user
const mockUserData: Record<
  string,
  {
    displayName: string;
    nickname: string;
    avatar?: string;
    joinedDate: string;
    folders: Folder[];
  }
> = {
  johndoe: {
    displayName: "John Doe",
    nickname: "johndoe",
    joinedDate: "January 2024",
    folders: [
      { folderId: "1", title: "Science", setCount: 5 },
      { folderId: "2", title: "Languages", setCount: 3 },
    ],
  },
};

export default function PublicProfileContent({
  nickname,
  initialSets,
}: {
  nickname: string;
  initialSets: FlashcardSet[];
}) {
  const [activeTab, setActiveTab] = useState("sets");
  const [searchQuery, setSearchQuery] = useState("");

  const flashcardSets = initialSets;

  // Get user data
  const userData = mockUserData[nickname] || {
    displayName: nickname,
    nickname: nickname,
    joinedDate: "Unknown",
    folders: [
      { folderId: "m1", title: "Mock Folder 1", setCount: 5 },
      { folderId: "m2", title: "Mock Folder 2", setCount: 3 },
    ],
  };

  // Filter sets and folders based on search query
  const filteredSets = flashcardSets.filter((set) =>
    set.setName?.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  const filteredFolders = userData.folders.filter((folder) =>
    folder.title.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  const joinedDateToDisplay =
    flashcardSets.length > 0 && flashcardSets[0].user?.createdAt
      ? new Date(flashcardSets[0].user.createdAt).toLocaleDateString("en-US", {
          month: "long",
          year: "numeric",
        })
      : userData.joinedDate;

  const displayedNickname =
    flashcardSets.length > 0 && flashcardSets[0].user?.nickname
      ? flashcardSets[0].user.nickname
      : userData.displayName;

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* Profile Header */}
        <div className="mb-8 flex flex-col items-center text-center">
          <Avatar className="h-24 w-24 border-4 border-pink-200">
            <AvatarImage
              src={userData.avatar || "/placeholder.svg?height=96&width=96"}
              alt={displayedNickname}
            />
            <AvatarFallback className="bg-pink-100 text-3xl text-pink-500">
              {displayedNickname
                .split(" ")
                .map((n) => n[0])
                .join("")
                .toUpperCase()}
            </AvatarFallback>
          </Avatar>
          <h1 className="mt-4 text-2xl font-bold text-gray-800">
            {displayedNickname}
          </h1>
          <p className="text-gray-500">@{displayedNickname}</p>
          <div className="mt-2 flex items-center gap-2 text-sm text-gray-400">
            <Calendar className="h-4 w-4" />
            <span>Joined {joinedDateToDisplay}</span>
          </div>
        </div>

        {/* Tabs Section */}
        <Tabs
          value={activeTab}
          onValueChange={(value) => {
            setActiveTab(value);
            setSearchQuery("");
          }}
          className="w-full"
        >
          <TabsList className="mb-6 h-auto w-full justify-center gap-2 border-b border-gray-200 bg-transparent p-0">
            <TabsTrigger
              value="sets"
              className={cn(
                "rounded-none border-0 border-b-2 border-transparent bg-transparent px-6 py-3 text-gray-600 shadow-none",
                "data-[state=active]:border-b-pink-500 data-[state=active]:bg-transparent data-[state=active]:text-pink-500 data-[state=active]:shadow-none",
                "focus-visible:ring-0 focus-visible:ring-offset-0",
              )}
            >
              <BookOpen className="mr-2 h-4 w-4" />
              Sets ({flashcardSets.length})
            </TabsTrigger>

            <TabsTrigger
              value="folders"
              className={cn(
                "rounded-none border-0 border-b-2 border-transparent bg-transparent px-6 py-3 text-gray-600 shadow-none",
                "data-[state=active]:border-b-pink-500 data-[state=active]:bg-transparent data-[state=active]:text-pink-500 data-[state=active]:shadow-none",
                "focus-visible:ring-0 focus-visible:ring-offset-0",
              )}
            >
              <FolderOpen className="mr-2 h-4 w-4" />
              Folders ({filteredFolders.length})
            </TabsTrigger>
          </TabsList>

          <TabsContent value="sets" className="mt-0">
            {/* Search Bar for Sets */}
            <div className="mb-6">
              <div className="relative mx-auto max-w-2xl">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
                <Input
                  type="text"
                  placeholder="Search sets..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10 border-gray-200 focus:border-pink-300 focus:ring-pink-200"
                />
              </div>
            </div>

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
                  searchQuery
                    ? "Try a different search term"
                    : "This user hasn't created any flashcard sets yet"
                }
              />
            )}
          </TabsContent>

          <TabsContent value="folders" className="mt-0">
            {/* Search Bar for Folders */}
            <div className="mb-6">
              <div className="relative mx-auto max-w-2xl">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
                <Input
                  type="text"
                  placeholder="Search folders..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10 border-gray-200 focus:border-pink-300 focus:ring-pink-200"
                />
              </div>
            </div>
            {filteredFolders.length > 0 ? (
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
                  searchQuery
                    ? "Try a different search term"
                    : "This user hasn't created any folders yet"
                }
              />
            )}
          </TabsContent>
        </Tabs>
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
  return (
    <Card className="group border-gray-200 bg-white transition-all hover:border-pink-200 hover:shadow-md">
      <CardContent className="p-4">
        <div className="mb-3">
          <Link
            href={`/users/${nickname}/sets/${set.setId}`}
            className="font-semibold text-gray-800 hover:text-pink-500"
          >
            {set.setName}
          </Link>
          <p className="mt-1 text-sm text-gray-500">{set.termCount} terms</p>
        </div>
        <div className="flex items-center gap-2">
          <Avatar className="h-6 w-6">
            <AvatarFallback className="bg-pink-100 text-xs text-pink-500">
              <User className="h-3 w-3" />
            </AvatarFallback>
          </Avatar>
          <Link
            href={`/users/${nickname}/profile`}
            className="text-sm text-gray-500 hover:text-pink-500"
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
  folder: Folder;
  nickname: string;
}) {
  return (
    <Card className="group cursor-pointer border-gray-200 bg-white transition-all hover:border-pink-200 hover:shadow-md">
      <CardContent className="p-4">
        <div className="mb-3 flex items-center gap-3">
          <div className="rounded-lg bg-pink-100 p-2">
            <FolderOpen className="h-5 w-5 text-pink-500" />
          </div>
          <div>
            <h3 className="font-semibold text-gray-800">{folder.title}</h3>
            <p className="text-sm text-gray-500">{folder.setCount} sets</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Avatar className="h-6 w-6">
            <AvatarFallback className="bg-pink-100 text-xs text-pink-500">
              <User className="h-3 w-3" />
            </AvatarFallback>
          </Avatar>
          <Link
            href={`/users/${nickname}/profile`}
            className="text-sm text-gray-500 hover:text-pink-500"
          >
            {nickname}
          </Link>
        </div>
      </CardContent>
    </Card>
  );
}

function EmptyState({
  icon: Icon,
  title,
  description,
}: {
  icon: React.ElementType;
  title: string;
  description: string;
}) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      <div className="mb-4 rounded-full bg-gray-100 p-4">
        <Icon className="h-8 w-8 text-gray-400" />
      </div>
      <h3 className="text-lg font-medium text-gray-800">{title}</h3>
      <p className="mt-1 text-sm text-gray-500">{description}</p>
    </div>
  );
}
