"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { ArrowLeft, Loader2, Search, User } from "lucide-react";
import { toast } from "sonner";

import { getUserFlashcardSetsByNickname } from "@/api/userService";
import EmptySetsState from "@/components/sets/empty-sets-state";
import StudySetCard, {
  type StudySetListItem,
} from "@/components/sets/study-set-card";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

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
    let isCancelled = false;

    async function fetchUserSets() {
      try {
        setIsLoading(true);

        const setsData = await getUserFlashcardSetsByNickname(nickname);

        if (isCancelled) {
          return;
        }

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
      } catch (error: unknown) {
        if (isCancelled) {
          return;
        }

        console.error("Error fetching user sets:", error);

        const message =
          error instanceof Error ? error.message : "Failed to load user sets.";

        toast.error(message);
        setSets([]);
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    }

    void fetchUserSets();

    return () => {
      isCancelled = true;
    };
  }, [nickname]);

  const normalizedSearchQuery = searchQuery.trim().toLowerCase();

  const filteredSets = useMemo(() => {
    if (!normalizedSearchQuery) {
      return sets;
    }

    return sets.filter(
      (set) =>
        set.title.toLowerCase().includes(normalizedSearchQuery) ||
        set.description.toLowerCase().includes(normalizedSearchQuery),
    );
  }, [sets, normalizedSearchQuery]);

  const encodedNickname = encodeURIComponent(nickname);

  return (
    <main className="min-h-[calc(100svh-4rem)] bg-background text-foreground">
      <div className="container mx-auto px-4 py-8">
        <Button
          variant="ghost"
          className="mb-6 text-muted-foreground hover:bg-pink-50 hover:text-pink-500 dark:hover:bg-pink-950/40 dark:hover:text-pink-400"
          asChild
        >
          <Link href={`/users/${encodedNickname}/profile`}>
            <ArrowLeft className="mr-2 h-4 w-4" aria-hidden="true" />
            Back to profile
          </Link>
        </Button>

        <div className="mb-8">
          <div className="mb-3 flex items-center gap-3">
            <Avatar className="h-10 w-10">
              <AvatarFallback className="bg-pink-100 text-pink-500 dark:bg-pink-950/50 dark:text-pink-400">
                <User className="h-5 w-5" aria-hidden="true" />
              </AvatarFallback>
            </Avatar>

            <div className="min-w-0">
              <h1 className="break-words text-3xl font-bold text-foreground">
                {nickname}&apos;s sets
              </h1>

              <p className="mt-1 text-muted-foreground">
                Browse flashcard sets created by this user.
              </p>
            </div>
          </div>
        </div>

        <div className="relative mb-6">
          <Search
            className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
            aria-hidden="true"
          />

          <Input
            type="search"
            value={searchQuery}
            onChange={(event) => setSearchQuery(event.target.value)}
            placeholder="Search user's sets..."
            className="border-input bg-background pl-10 text-foreground placeholder:text-muted-foreground focus-visible:border-pink-300 focus-visible:ring-pink-500/20 dark:focus-visible:border-pink-800"
            aria-label={`Search ${nickname}'s sets`}
          />
        </div>

        {isLoading ? (
          <div
            className="flex items-center justify-center gap-3 py-10 text-muted-foreground"
            role="status"
          >
            <Loader2
              className="h-5 w-5 animate-spin text-pink-500 dark:text-pink-400"
              aria-hidden="true"
            />

            <span>Downloading study sets...</span>
          </div>
        ) : filteredSets.length > 0 ? (
          <div className="grid gap-4 md:grid-cols-2">
            {filteredSets.map((set) => (
              <StudySetCard key={set.id} set={set} />
            ))}
          </div>
        ) : (
          <EmptySetsState
            title={
              normalizedSearchQuery
                ? "No sets found"
                : "This user has no sets yet"
            }
            description={
              normalizedSearchQuery
                ? "Try using a different search phrase."
                : "There are no public flashcard sets to display."
            }
          />
        )}
      </div>
    </main>
  );
}
