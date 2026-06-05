"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { ArrowLeft, Search, User } from "lucide-react";
import { toast } from "sonner";

import { getUserFlashcardSetsByNickname } from "@/api/userService";

import StudySetCard, {
  type StudySetListItem,
} from "@/components/sets/study-set-card";
import EmptySetsState from "@/components/sets/empty-sets-state";

import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";

interface UserSetsPageProps {
  nickname: string;
}

interface FlashcardSetDTO {
  setId: number;
  user: {
    nickname: string;
  };
  setName: string;
  description: string;
  termCount: number;
}

type StudySet = StudySetListItem;

export default function UserSetsPage({ nickname }: UserSetsPageProps) {
  const [sets, setSets] = useState<StudySet[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchUserSets = async () => {
      try {
        setIsLoading(true);

        const setsData = await getUserFlashcardSetsByNickname(nickname);

        const formattedSets: StudySet[] = setsData.map(
          (set: FlashcardSetDTO) => ({
            id: set.setId.toString(),
            title: set.setName,
            description: set.description || "",
            termCount: Number(set.termCount),
            nickname: set.user.nickname,
          }),
        );

        setSets(formattedSets);
      } catch (error) {
        console.error("Error fetching user sets:", error);

        toast.error("Failed to load user sets.", {
          style: {
            background: "red",
            color: "white",
          },
        });

        setSets([]);
      } finally {
        setIsLoading(false);
      }
    };

    fetchUserSets();
  }, [nickname]);

  const filteredSets = useMemo(() => {
    const normalizedQuery = searchQuery.toLowerCase().trim();

    if (!normalizedQuery) return sets;

    return sets.filter(
      (set) =>
        set.title.toLowerCase().includes(normalizedQuery) ||
        set.description.toLowerCase().includes(normalizedQuery),
    );
  }, [sets, searchQuery]);

  return (
    <div className="container mx-auto px-4 py-8">
      <Button
        variant="ghost"
        className="mb-6 text-gray-500 hover:text-pink-500"
        asChild
      >
        <Link href={`/profile/users/${encodeURIComponent(nickname)}`}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to profile
        </Link>
      </Button>

      <div className="mb-8">
        <div className="mb-3 flex items-center gap-3">
          <Avatar className="h-10 w-10">
            <AvatarFallback className="bg-pink-100 text-pink-500">
              <User className="h-5 w-5" />
            </AvatarFallback>
          </Avatar>

          <div>
            <h1 className="text-3xl font-bold text-gray-800">
              {nickname}&apos;s sets
            </h1>

            <p className="mt-1 text-gray-500">
              Browse flashcard sets created by this user.
            </p>
          </div>
        </div>
      </div>

      <div className="relative mb-6">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />

        <Input
          value={searchQuery}
          onChange={(event) => setSearchQuery(event.target.value)}
          placeholder="Search user's sets..."
          className="border-gray-200 pl-10 focus:border-pink-300 focus:ring-pink-200"
        />
      </div>

      {isLoading ? (
        <div className="py-10 text-center text-gray-500">
          Downloading study sets...
        </div>
      ) : filteredSets.length > 0 ? (
        <div className="grid gap-4 md:grid-cols-2">
          {filteredSets.map((set) => (
            <StudySetCard key={set.id} set={set} />
          ))}
        </div>
      ) : (
        <EmptySetsState
          title={searchQuery ? "No sets found" : "This user has no sets yet"}
          description={
            searchQuery
              ? "Try using a different search phrase."
              : "There are no public flashcard sets to display."
          }
        />
      )}
    </div>
  );
}
