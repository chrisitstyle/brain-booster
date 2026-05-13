const BASE_API_URL = process.env.NEXT_PUBLIC_API_URL;

export interface CreateFlashcardData {
  setId: number;
  term: string;
  definition: string;
}

export interface Flashcard {
  flashcardId: number;
  setId: number;
  term: string;
  definition: string;
  starred?: boolean;
}

export interface UpdateFlashcardData {
  term: string;
  definition: string;
}

export async function addFlashcard(data: CreateFlashcardData, token: string) {
  const response = await fetch(`${BASE_API_URL}/flashcards`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    throw new Error("Failed to create flashcard");
  }

  return await response.json();
}

export async function getFlashcardsBySetId(
  setId: string | number,
): Promise<Flashcard[]> {
  const response = await fetch(
    `${BASE_API_URL}/flashcard-sets/${setId}/flashcards`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    },
  );

  if (!response.ok) {
    throw new Error("Failed to fetch flashcards");
  }

  return await response.json();
}

export async function updateFlashcard(
  flashcardId: number,
  data: UpdateFlashcardData,
  token: string,
) {
  const response = await fetch(`${BASE_API_URL}/flashcards/${flashcardId}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    throw new Error("Failed to update flashcard");
  }

  return await response.json();
}
