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
import { useState } from "react";

interface StudySet {
  id: string;
  title: string;
  termCount: number;
  author: string;
  lastStudied?: string;
}

interface Folder {
  id: string;
  title: string;
  setCount: number;
}

const recentSets: StudySet[] = [
  {
    id: "1",
    title: "Biology Chapter 5 - Cell Division",
    termCount: 45,
    author: "You",
    lastStudied: "2 hours ago",
  },
  {
    id: "2",
    title: "Spanish Vocabulary - Unit 3",
    termCount: 60,
    author: "You",
    lastStudied: "Yesterday",
  },
  {
    id: "3",
    title: "History - World War II",
    termCount: 82,
    author: "You",
    lastStudied: "3 days ago",
  },
  {
    id: "4",
    title: "Chemistry - Periodic Table",
    termCount: 118,
    author: "You",
    lastStudied: "1 week ago",
  },
];

const folders: Folder[] = [
  { id: "1", title: "Science", setCount: 5 },
  { id: "2", title: "Languages", setCount: 3 },
  { id: "3", title: "History", setCount: 4 },
];

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

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Profile Header */}
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
          >
            <Plus className="mr-2 h-4 w-4" />
            Create set
          </Button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="mb-8 grid gap-4 md:grid-cols-3">
        {achievements.map((achievement, index) => (
          <Card key={index} className="border-gray-200 bg-white">
            <CardContent className="flex items-center gap-4 p-6">
              <div
                className={cn("rounded-full bg-gray-50 p-3", achievement.color)}
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

      {/* Weekly Progress */}
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
                      "h-8 w-8 rounded-full flex items-center justify-center text-xs font-medium",
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

      {/* Tabs Section */}
      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
        <TabsList className="mb-6 w-full justify-start gap-2 border-b border-gray-200 bg-transparent p-0">
          <TabsTrigger
            value="sets"
            className={cn(
              "rounded-none border-b-2 border-transparent px-4 py-3 text-gray-600 data-[state=active]:border-pink-500 data-[state=active]:bg-transparent data-[state=active]:text-pink-500 data-[state=active]:shadow-none",
            )}
          >
            <BookOpen className="mr-2 h-4 w-4" />
            Study Sets
          </TabsTrigger>
          <TabsTrigger
            value="folders"
            className={cn(
              "rounded-none border-b-2 border-transparent px-4 py-3 text-gray-600 data-[state=active]:border-pink-500 data-[state=active]:bg-transparent data-[state=active]:text-pink-500 data-[state=active]:shadow-none",
            )}
          >
            <FolderOpen className="mr-2 h-4 w-4" />
            Folders
          </TabsTrigger>
          <TabsTrigger
            value="recent"
            className={cn(
              "rounded-none border-b-2 border-transparent px-4 py-3 text-gray-600 data-[state=active]:border-pink-500 data-[state=active]:bg-transparent data-[state=active]:text-pink-500 data-[state=active]:shadow-none",
            )}
          >
            <Clock className="mr-2 h-4 w-4" />
            Recent
          </TabsTrigger>
        </TabsList>

        <TabsContent value="sets" className="mt-0">
          <div className="grid gap-4 md:grid-cols-2">
            {recentSets.map((set) => (
              <StudySetCard key={set.id} set={set} />
            ))}
          </div>
        </TabsContent>

        <TabsContent value="folders" className="mt-0">
          <div className="grid gap-4 md:grid-cols-3">
            {folders.map((folder) => (
              <FolderCard key={folder.id} folder={folder} />
            ))}
            <Card className="flex cursor-pointer items-center justify-center border-2 border-dashed border-gray-200 bg-white p-6 transition-colors hover:border-pink-300 hover:bg-pink-50">
              <div className="text-center">
                <Plus className="mx-auto mb-2 h-8 w-8 text-gray-400" />
                <p className="text-sm font-medium text-gray-500">
                  Create new folder
                </p>
              </div>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="recent" className="mt-0">
          <div className="space-y-3">
            {recentSets.map((set) => (
              <RecentActivityItem key={set.id} set={set} />
            ))}
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
}

function StudySetCard({ set }: { set: StudySet }) {
  return (
    <Card className="group border-gray-200 bg-white transition-all hover:border-pink-200 hover:shadow-md">
      <CardContent className="p-4">
        <div className="mb-3 flex items-start justify-between">
          <div className="flex-1">
            <Link
              href={`/sets/${set.id}`}
              className="font-semibold text-gray-800 hover:text-pink-500"
            >
              {set.title}
            </Link>
            <p className="mt-1 text-sm text-gray-500">{set.termCount} terms</p>
          </div>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                className="h-8 w-8 opacity-0 group-hover:opacity-100"
              >
                <MoreHorizontal className="h-4 w-4 text-gray-400" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem>Edit</DropdownMenuItem>
              <DropdownMenuItem>Share</DropdownMenuItem>
              <DropdownMenuItem>Add to folder</DropdownMenuItem>
              <DropdownMenuItem className="text-red-500">
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

function FolderCard({ folder }: { folder: Folder }) {
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
            href={`/sets/${set.id}`}
            className="font-medium text-gray-800 hover:text-pink-500"
          >
            {set.title}
          </Link>
          <p className="text-sm text-gray-500">{set.termCount} terms</p>
        </div>
      </div>
      <div className="flex items-center gap-4">
        <span className="text-sm text-gray-400">{set.lastStudied}</span>
        <Button size="sm" className="bg-pink-500 text-white hover:bg-pink-600">
          Study
        </Button>
      </div>
    </div>
  );
}
