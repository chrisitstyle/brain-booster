import { notFound, redirect } from "next/navigation";

import { getFlashcardsBySetId, type Flashcard } from "@/api/flashcardService";
import {
  getFlashcardSetById,
  type FlashcardSet,
} from "@/api/flashcardSetService";

import StudyFlashcardSetClient from "./study-flashcardset-client";

interface StudySetPageProps {
  params: Promise<{
    nickname: string;
    id: string;
  }>;
}

export default async function StudySetPage({ params }: StudySetPageProps) {
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
  } catch (error) {
    console.error("Failed to load flashcard set:", error);
    notFound();
  }

  if (!fetchedSet) {
    notFound();
  }

  const ownerNickname = fetchedSet.user.nickname;

  /*
   * Gdy nickname właściciela został zmieniony albo użytkownik podał
   * niepoprawny nickname w adresie, przekieruj na właściwy URL.
   */
  if (nickname !== ownerNickname) {
    redirect(
      `/users/${encodeURIComponent(ownerNickname)}/sets/${fetchedSet.setId}`,
    );
  }

  return (
    <StudyFlashcardSetClient
      studySet={fetchedSet}
      initialFlashcards={fetchedCards}
    />
  );
}
