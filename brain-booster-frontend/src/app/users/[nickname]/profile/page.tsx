"use client";

import * as React from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { BookOpen, Calendar, FolderOpen, Search, User } from "lucide-react";

import { cn } from "@/lib/utils";
import { Input } from "@/components/ui/input";
import { Card, CardContent } from "@/components/ui/card";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

interface StudySet {
  id: string;
  title: string;
  termCount: number;
}

interface Folder {
  id: string;
  title: string;
  setCount: number;
}

// Mock data - in real app this would come from API
const mockUserData: Record<
  string,
  {
    displayName: string;
    username: string;
    avatar?: string;
    joinedDate: string;
    sets: StudySet[];
    folders: Folder[];
  }
> = {
  johndoe: {
    displayName: "John Doe",
    username: "johndoe",
    joinedDate: "January 2024",
    sets: [
      { id: "1", title: "Biology Chapter 5 - Cell Division", termCount: 45 },
      { id: "2", title: "Spanish Vocabulary - Unit 3", termCount: 60 },
      { id: "3", title: "History - World War II", termCount: 82 },
      { id: "4", title: "Chemistry - Periodic Table", termCount: 118 },
    ],
    folders: [
      { id: "1", title: "Science", setCount: 5 },
      { id: "2", title: "Languages", setCount: 3 },
    ],
  },
  janedoe: {
    displayName: "Jane Doe",
    username: "janedoe",
    joinedDate: "March 2023",
    sets: [
      { id: "1", title: "French Basics", termCount: 30 },
      { id: "2", title: "Algebra Formulas", termCount: 25 },
      { id: "3", title: "Psychology 101", termCount: 50 },
    ],
    folders: [
      { id: "1", title: "Math", setCount: 4 },
      { id: "2", title: "Languages", setCount: 2 },
      { id: "3", title: "Psychology", setCount: 3 },
    ],
  },
};

export default function PublicProfilePage() {
  const params = useParams();
  const username = params.nickname as string;
  const [activeTab, setActiveTab] = React.useState("sets");
  const [searchQuery, setSearchQuery] = React.useState("");

  // Get user data (in real app this would be fetched from API)
  const userData = mockUserData[username] || {
    displayName: username,
    username: username,
    joinedDate: "Unknown",
    sets: [],
    folders: [],
  };

  // Filter sets and folders based on search query
  const filteredSets = userData.sets.filter((set) =>
    set.title.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  const filteredFolders = userData.folders.filter((folder) =>
    folder.title.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        {/* Profile Header */}
        <div className="mb-8 flex flex-col items-center text-center">
          <Avatar className="h-24 w-24 border-4 border-pink-200">
            <AvatarImage
              src={userData.avatar || "/placeholder.svg?height=96&width=96"}
              alt={userData.displayName}
            />
            <AvatarFallback className="bg-pink-100 text-3xl text-pink-500">
              {userData.displayName
                .split(" ")
                .map((n) => n[0])
                .join("")
                .toUpperCase()}
            </AvatarFallback>
          </Avatar>
          <h1 className="mt-4 text-2xl font-bold text-gray-800">
            {userData.displayName}
          </h1>
          <p className="text-gray-500">@{userData.username}</p>
          <div className="mt-2 flex items-center gap-2 text-sm text-gray-400">
            <Calendar className="h-4 w-4" />
            <span>Joined {userData.joinedDate}</span>
          </div>
        </div>

        {/* Search Bar */}
        <div className="mb-6">
          <div className="relative mx-auto max-w-md">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
            <Input
              type="text"
              placeholder="Filter by title..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-10 border-gray-200 focus:border-pink-300 focus:ring-pink-200"
            />
          </div>
        </div>

        {/* Tabs Section */}
        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
          <TabsList className="mb-6 w-full justify-center gap-2 border-b border-gray-200 bg-transparent p-0">
            <TabsTrigger
              value="sets"
              className={cn(
                "rounded-none border-b-2 border-transparent px-6 py-3 text-gray-600 data-[state=active]:border-pink-500 data-[state=active]:bg-transparent data-[state=active]:text-pink-500 data-[state=active]:shadow-none",
              )}
            >
              <BookOpen className="mr-2 h-4 w-4" />
              Flashcard Sets ({filteredSets.length})
            </TabsTrigger>
            <TabsTrigger
              value="folders"
              className={cn(
                "rounded-none border-b-2 border-transparent px-6 py-3 text-gray-600 data-[state=active]:border-pink-500 data-[state=active]:bg-transparent data-[state=active]:text-pink-500 data-[state=active]:shadow-none",
              )}
            >
              <FolderOpen className="mr-2 h-4 w-4" />
              Folders ({filteredFolders.length})
            </TabsTrigger>
          </TabsList>

          <TabsContent value="sets" className="mt-0">
            {filteredSets.length > 0 ? (
              <div className="mx-auto max-w-2xl space-y-4">
                {filteredSets.map((set) => (
                  <PublicStudySetCard
                    key={set.id}
                    set={set}
                    username={userData.username}
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
            {filteredFolders.length > 0 ? (
              <div className="mx-auto max-w-2xl space-y-4">
                {filteredFolders.map((folder) => (
                  <PublicFolderCard
                    key={folder.id}
                    folder={folder}
                    username={userData.username}
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
  username,
}: {
  set: StudySet;
  username: string;
}) {
  return (
    <Card className="group border-gray-200 bg-white transition-all hover:border-pink-200 hover:shadow-md">
      <CardContent className="p-4">
        <div className="mb-3">
          <Link
            href={`/sets/${set.id}`}
            className="font-semibold text-gray-800 hover:text-pink-500"
          >
            {set.title}
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
            href={`/users/${username}`}
            className="text-sm text-gray-500 hover:text-pink-500"
          >
            {username}
          </Link>
        </div>
      </CardContent>
    </Card>
  );
}

function PublicFolderCard({
  folder,
  username,
}: {
  folder: Folder;
  username: string;
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
            href={`/users/${username}`}
            className="text-sm text-gray-500 hover:text-pink-500"
          >
            {username}
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
