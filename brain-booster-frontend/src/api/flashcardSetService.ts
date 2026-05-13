const BASE_API_URL = process.env.NEXT_PUBLIC_API_URL;

export interface CreateFlashcardSetData {
  userId: number;
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

export async function addFlashcardSet(
  data: CreateFlashcardSetData,
  token: string,
) {
  const response = await fetch(`${BASE_API_URL}/flashcard-sets`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    let errMsg: string;
    const text = await response.text();
    try {
      const { message } = JSON.parse(text);
      errMsg = message || "Failed to create study set";
    } catch {
      errMsg = text || "Failed to create study set";
    }
    throw new Error(errMsg);
  }

  const result = await response.json();
  return result;
}

export const deleteFlashcardSet = async (setId: string, token: string) => {
  const response = await fetch(`${BASE_API_URL}/flashcard-sets/${setId}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error(
      `Failed to delete flashcard set with status: ${response.status}`,
    );
  }
};

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
    throw new Error("Failed to fetch flashcard set details");
  }

  return await response.json();
}
