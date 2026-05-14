import { notFound } from "next/navigation";
import { getFlashcardsBySetId, type Flashcard } from "@/api/flashcardService";
import {
  getFlashcardSetById,
  type FlashcardSet,
} from "@/api/flashcardSetService";
import StudyFlashcardSetClient from "./study-flashcardset-client";

export default async function StudySetPage({
  params,
}: {
  params: Promise<{ nickname: string; id: string }>;
}) {
  const { nickname, id } = await params;

  let fetchedSet: FlashcardSet | null = null;
  let fetchedCards: Flashcard[] = [];

  try {
    const [set, cards] = await Promise.all([
      getFlashcardSetById(id),
      getFlashcardsBySetId(id),
    ]);

    fetchedSet = set;
    fetchedCards = cards;
  } catch {
    notFound();
  }

  if (!fetchedSet) {
    notFound();
  }

  return (
    <StudyFlashcardSetClient
      studySet={fetchedSet}
      initialFlashcards={fetchedCards}
      nickname={nickname}
    />
  );
}
