"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { getFlashcardsBySetId, type Flashcard } from "@/api/flashcardService";
import TestGame from "@/components/games/TestGame";

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

  if (isLoading) {
    return <div className="p-8 text-gray-600">Loading test mode...</div>;
  }

  if (errorMessage) {
    return <div className="p-8 text-red-500">{errorMessage}</div>;
  }

  return (
    <main className="p-8">
      <TestGame flashcards={flashcards} />
    </main>
  );
}
