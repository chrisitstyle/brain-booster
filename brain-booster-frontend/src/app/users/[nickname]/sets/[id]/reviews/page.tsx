import { notFound } from "next/navigation";

import ReviewSessionClient from "./review-session-client";

interface ReviewPageProps {
  params: Promise<{
    nickname: string;
    id: string;
  }>;
}

/**
 * Resolves the selected flashcard set for the FSRS review page.
 */
export default async function ReviewPage({ params }: ReviewPageProps) {
  const { nickname, id } = await params;

  const flashcardSetId = Number(id);

  if (!Number.isSafeInteger(flashcardSetId) || flashcardSetId <= 0) {
    notFound();
  }

  return (
    <ReviewSessionClient
      flashcardSetId={flashcardSetId}
      ownerNickname={nickname}
    />
  );
}
