const BASE_API_URL = process.env.NEXT_PUBLIC_API_URL;

export interface CreateFlashcardSetData {
  userId: number;
  setName: string;
  description: string;
}

export interface UpdateFlashcardSetData {
  setName: string;
  description: string;
}

export interface FlashcardSet {
  setId: number;
  user: {
    nickname: string;
    createdAt: string;
  };
  setName: string;
  description: string;
  createdAt: string;
  termCount: number;
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

export async function addFlashcardSet(
  data: CreateFlashcardSetData,
  token: string,
): Promise<FlashcardSet> {
  const response = await fetch(`${BASE_API_URL}/flashcard-sets`, {
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
      "Failed to create study set",
    );
    throw new Error(message);
  }

  return await response.json();
}

export async function getFlashcardSetById(
  setId: string | number,
): Promise<FlashcardSet> {
  const response = await fetch(`${BASE_API_URL}/flashcard-sets/${setId}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    const message = await getErrorMessage(
      response,
      "Failed to fetch flashcard set details",
    );
    throw new Error(message);
  }

  return await response.json();
}

export async function updateFlashcardSetById(
  setId: string | number,
  data: UpdateFlashcardSetData,
  token: string,
): Promise<FlashcardSet> {
  const response = await fetch(`${BASE_API_URL}/flashcard-sets/${setId}`, {
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
      "Failed to update flashcard set",
    );
    throw new Error(message);
  }

  return await response.json();
}

export async function deleteFlashcardSet(
  setId: string | number,
  token: string,
): Promise<void> {
  const response = await fetch(`${BASE_API_URL}/flashcard-sets/${setId}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    const message = await getErrorMessage(
      response,
      `Failed to delete flashcard set with status: ${response.status}`,
    );
    throw new Error(message);
  }
}
