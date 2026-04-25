const BASE_API_URL = process.env.NEXT_PUBLIC_API_URL;

export const getUserFlashcardSets = async (userId: number, token: string) => {
  const response = await fetch(
    `${BASE_API_URL}/users/${userId}/flashcard-sets`,
    {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    },
  );

  if (!response.ok) {
    throw new Error(
      `Error: ${response.status} - Cannot fetch flashcard sets for user with this ID`,
    );
  }

  return response.json();
};
