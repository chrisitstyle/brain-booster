"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";

import {
  BrainCircuit,
  FileQuestion,
  Keyboard,
  Layers,
  MousePointerClick,
} from "lucide-react";

import { getMyGameResultsBySetId } from "@/api/game-results/gameResultService";
import { getCurrentUser } from "@/api/profileService";
import { useAuth } from "@/context/AuthContext";

import type { GameMode, GameResult } from "@/types/games";

interface StudyGamesSectionProps {
  nickname: string;
  setId: number | string;
}

interface GameResultsState {
  data: GameResult[];
  dataKey: string | null;
  errorKey: string | null;
}

interface ReviewAccessState {
  token: string;
  ownerNickname: string;
  isOwner: boolean;
}

const games: {
  name: string;
  href: string;
  mode: GameMode;
  icon: typeof MousePointerClick;
}[] = [
  {
    name: "Multiple choice",
    href: "quiz",
    mode: "multiple-choice",
    icon: MousePointerClick,
  },
  {
    name: "Written",
    href: "typing",
    mode: "written",
    icon: Keyboard,
  },
  {
    name: "Matching",
    href: "match",
    mode: "matching",
    icon: Layers,
  },
  {
    name: "Custom test",
    href: "test",
    mode: "custom-test",
    icon: FileQuestion,
  },
];

// Keep all study mode cards visually consistent with the existing UI.
const studyModeCardClassName =
  "group flex min-h-24 flex-col items-center justify-center " +
  "rounded-xl border border-border bg-card px-3 py-4 text-center " +
  "text-card-foreground shadow-sm transition-all " +
  "hover:-translate-y-0.5 hover:border-pink-200 " +
  "hover:bg-pink-50/50 hover:shadow-md " +
  "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring " +
  "dark:hover:border-pink-900 dark:hover:bg-pink-950/20";

const studyModeIconClassName =
  "mb-2 flex h-8 w-8 items-center justify-center rounded-lg " +
  "bg-pink-100 text-pink-500 transition-colors " +
  "group-hover:bg-pink-500 group-hover:text-white " +
  "dark:bg-pink-950/50 dark:text-pink-400 " +
  "dark:group-hover:bg-pink-500 dark:group-hover:text-white";

const studyModeTitleClassName =
  "px-2 text-xs font-semibold text-card-foreground " +
  "transition-colors group-hover:text-pink-600 " +
  "dark:group-hover:text-pink-400";

const studyModeStatusClassName =
  "mt-1 max-w-full truncate rounded-full bg-muted " +
  "px-2 py-0.5 text-[11px] font-medium text-muted-foreground " +
  "transition-colors group-hover:bg-pink-100 " +
  "group-hover:text-pink-500 dark:group-hover:bg-pink-950/60 " +
  "dark:group-hover:text-pink-400";

/**
 * Formats the latest result for a completed study game.
 */
function getResultLabel(result?: GameResult): string {
  if (!result) {
    return "Not completed";
  }

  const accuracyPercentage =
    result.totalQuestions > 0
      ? Math.round((result.score / result.totalQuestions) * 100)
      : 0;

  return (
    `Score: ${result.score} / ${result.totalQuestions}` +
    ` · ${accuracyPercentage}%`
  );
}

/**
 * Displays available study modes and the user's latest game results.
 *
 * The FSRS review mode is displayed only when the authenticated user
 * owns the flashcard set. The backend independently verifies ownership
 * before allowing access to reviews.
 */
export default function StudyGamesSection({
  nickname,
  setId,
}: StudyGamesSectionProps) {
  const { token } = useAuth();

  const numericSetId = useMemo(() => {
    const parsedSetId = Number(setId);

    return Number.isSafeInteger(parsedSetId) && parsedSetId > 0
      ? parsedSetId
      : null;
  }, [setId]);

  const queryKey =
    token && numericSetId ? `game-results:${numericSetId}:${token}` : null;

  const [gameResultsState, setGameResultsState] = useState<GameResultsState>({
    data: [],
    dataKey: null,
    errorKey: null,
  });

  const [reviewAccessState, setReviewAccessState] =
    useState<ReviewAccessState | null>(null);

  /**
   * Checks whether the authenticated user owns the current set.
   *
   * Ownership verification controls UI visibility only.
   * Spring Security remains responsible for enforcing permissions.
   */
  useEffect(() => {
    if (!token) {
      return;
    }

    const authenticatedToken = token;
    let isCancelled = false;

    async function checkReviewAccess() {
      try {
        const currentUser = await getCurrentUser(authenticatedToken);

        if (isCancelled) {
          return;
        }

        setReviewAccessState({
          token: authenticatedToken,
          ownerNickname: nickname,
          isOwner: currentUser.nickname === nickname,
        });
      } catch (error: unknown) {
        if (isCancelled) {
          return;
        }

        // Hide the review mode if ownership cannot be verified.
        console.error("Failed to verify flashcard set ownership:", error);

        setReviewAccessState({
          token: authenticatedToken,
          ownerNickname: nickname,
          isOwner: false,
        });
      }
    }

    void checkReviewAccess();

    return () => {
      isCancelled = true;
    };
  }, [token, nickname]);

  const canReviewFlashcardSet = Boolean(
    token &&
    numericSetId !== null &&
    reviewAccessState?.token === token &&
    reviewAccessState.ownerNickname === nickname &&
    reviewAccessState.isOwner,
  );

  const gameResults = useMemo(() => {
    if (!queryKey || gameResultsState.dataKey !== queryKey) {
      return [];
    }

    return gameResultsState.data;
  }, [queryKey, gameResultsState.dataKey, gameResultsState.data]);

  const isLoadingResults = Boolean(
    queryKey &&
    gameResultsState.dataKey !== queryKey &&
    gameResultsState.errorKey !== queryKey,
  );

  /**
   * Loads the user's latest results for the selected flashcard set.
   *
   * Stale responses are ignored when the token or set changes.
   */
  useEffect(() => {
    if (!token || !numericSetId || !queryKey) {
      return;
    }

    const authenticatedToken = token;
    const currentSetId = numericSetId;

    let isCancelled = false;

    async function loadGameResults() {
      try {
        const results = await getMyGameResultsBySetId(
          currentSetId,
          authenticatedToken,
        );

        if (isCancelled) {
          return;
        }

        setGameResultsState({
          data: results,
          dataKey: queryKey,
          errorKey: null,
        });
      } catch (error: unknown) {
        if (isCancelled) {
          return;
        }

        console.error("Failed to load game results:", error);

        setGameResultsState((previousState) => ({
          ...previousState,
          errorKey: queryKey,
        }));
      }
    }

    void loadGameResults();

    return () => {
      isCancelled = true;
    };
  }, [token, numericSetId, queryKey]);

  const resultByMode = useMemo(
    () => new Map(gameResults.map((result) => [result.mode, result])),
    [gameResults],
  );

  const encodedNickname = encodeURIComponent(nickname);

  const reviewHref =
    numericSetId !== null
      ? `/users/${encodedNickname}/sets/${numericSetId}/reviews`
      : null;

  return (
    <section className="mb-8 print:hidden">
      <div className="mx-auto max-w-2xl">
        <h2 className="mb-3 text-base font-bold text-foreground">
          Practice modes
        </h2>

        <div className="grid grid-cols-1 gap-2.5 sm:grid-cols-2">
          {games.map((game) => {
            const GameIcon = game.icon;
            const gameResult = resultByMode.get(game.mode);

            const gameHref =
              `/users/${encodedNickname}` + `/sets/${setId}/games/${game.href}`;

            const gameResultLabel = isLoadingResults
              ? "Loading..."
              : getResultLabel(gameResult);

            return (
              <Link
                key={game.mode}
                href={gameHref}
                className={studyModeCardClassName}
              >
                <div className={studyModeIconClassName}>
                  <GameIcon className="h-4 w-4" aria-hidden="true" />
                </div>

                <span className={studyModeTitleClassName}>{game.name}</span>

                {token && (
                  <span
                    className={studyModeStatusClassName}
                    title={gameResultLabel}
                  >
                    {gameResultLabel}
                  </span>
                )}
              </Link>
            );
          })}

          {/* FSRS is available only to the flashcard set owner */}
          {canReviewFlashcardSet && reviewHref && (
            <Link href={reviewHref} className={studyModeCardClassName}>
              <div className={studyModeIconClassName}>
                <BrainCircuit className="h-4 w-4" aria-hidden="true" />
              </div>

              <span className={studyModeTitleClassName}>Spaced repetition</span>

              <span className={studyModeStatusClassName}>FSRS reviews</span>
            </Link>
          )}
        </div>
      </div>
    </section>
  );
}
