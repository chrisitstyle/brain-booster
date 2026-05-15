import { notFound } from "next/navigation";
import { getFlashcardsBySetId, type Flashcard } from "@/api/flashcardService";
import {
  getFlashcardSetById,
  type FlashcardSet,
} from "@/api/flashcardSetService";
import EditFlashcardSetClient from "./edit-flashcard-set-client";

export default async function EditFlashcardSetPage({
  params,
}: {
  params: Promise<{ nickname: string; id: string }>;
}) {
  const { nickname, id } = await params;

  let set: FlashcardSet;
  let flashcards: Flashcard[];

  try {
    [set, flashcards] = await Promise.all([
      getFlashcardSetById(id),
      getFlashcardsBySetId(id),
    ]);
  } catch {
    notFound();
  }

  return (
    <EditFlashcardSetClient
      setId={id}
      nickname={nickname}
      initialSet={set}
      initialFlashcards={flashcards}
    />
  );
}
