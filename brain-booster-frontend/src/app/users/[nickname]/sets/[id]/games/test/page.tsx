"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { getFlashcardsBySetId, type Flashcard } from "@/api/flashcardService";
import TestGame from "@/components/games/TestGame";
import GamePageLayout from "@/components/games/shared/GamePageLayout";
import { getGameStorageKey } from "@/components/games/shared/game-storage";

export default function TestPage() {
  const params = useParams<{ nickname: string; id: string }>();

  const [flashcards, setFlashcards] = useState<Flashcard[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    async function loadFlashcards() {
      try {
        const data = await getFlashcardsBySetId(params.id);
        setFlashcards(data);
      } catch (error) {
        console.error(error);
        setErrorMessage("Failed to load flashcards.");
      } finally {
        setIsLoading(false);
      }
    }

    void loadFlashcards();
  }, [params.id]);

  return (
    <GamePageLayout
      nickname={params.nickname}
      setId={params.id}
      storageKeyToClearOnLeave={getGameStorageKey(params.id, "custom-test")}
    >
      {isLoading && <div className="text-gray-600">Loading custom test...</div>}

      {errorMessage && <div className="text-red-500">{errorMessage}</div>}

      {!isLoading && !errorMessage && (
        <TestGame flashcards={flashcards} setId={params.id} />
      )}
    </GamePageLayout>
  );
}
