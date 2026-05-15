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

async function getErrorMessage(response: Response, fallbackMessage: string) {
  const text = await response.text();

  try {
    const parsed = JSON.parse(text);
    return parsed.message || fallbackMessage;
  } catch {
    return text || fallbackMessage;
  }
}

export async function addFlashcard(
  data: CreateFlashcardData,
  token: string,
): Promise<Flashcard> {
  const response = await fetch(`${BASE_API_URL}/flashcards`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    const message = await getErrorMessage(
      response,
      "Failed to create flashcard",
    );
    throw new Error(message);
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
    const message = await getErrorMessage(
      response,
      "Failed to fetch flashcards",
    );
    throw new Error(message);
  }

  return await response.json();
}

export async function updateFlashcardById(
  flashcardId: string | number,
  data: UpdateFlashcardData,
  token: string,
): Promise<Flashcard> {
  const response = await fetch(`${BASE_API_URL}/flashcards/${flashcardId}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    const message = await getErrorMessage(
      response,
      "Failed to update flashcard",
    );
    throw new Error(message);
  }

  return await response.json();
}

export async function deleteFlashcard(
  flashcardId: string | number,
  token: string,
): Promise<void> {
  const response = await fetch(`${BASE_API_URL}/flashcards/${flashcardId}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    const message = await getErrorMessage(
      response,
      "Failed to delete flashcard",
    );
    throw new Error(message);
  }
}
