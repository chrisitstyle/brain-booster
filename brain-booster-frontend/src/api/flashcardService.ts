const BASE_API_URL = process.env.NEXT_PUBLIC_API_URL;

export interface CreateFlashcardData {
  setId: number;
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
