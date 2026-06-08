"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { getFlashcardsBySetId, type Flashcard } from "@/api/flashcardService";
import QuizGame from "@/components/games/QuizGame";
import GamePageLayout from "@/components/games/shared/GamePageLayout";

export default function QuizPage() {
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
    <GamePageLayout nickname={params.nickname} setId={params.id}>
      {isLoading && (
        <div className="text-gray-600">Loading multiple choice...</div>
      )}

      {errorMessage && <div className="text-red-500">{errorMessage}</div>}

      {!isLoading && !errorMessage && <QuizGame flashcards={flashcards} />}
    </GamePageLayout>
  );
}
