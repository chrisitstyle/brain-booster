const BASE_API_URL = process.env.NEXT_PUBLIC_API_URL;

export interface FlashcardSetInFolder {
  flashcardSetId: number;
  title: string;
  termCount: number;
}

export interface Folder {
  folderId: number;
  nickname: string;
  name: string;
  description: string;
  setCount: number;
  flashcardSets: FlashcardSetInFolder[];
}

export interface CreateFolderData {
  name: string;
  description: string;
}

export interface EditFolderData {
  folderId: number | string;
  name: string;
  description: string;
}

export interface AddSetToFolderData {
  folderId: number | string;
  flashcardSetId: number | string;
}

export async function createFolder(
  folderData: CreateFolderData,
  token: string,
): Promise<Folder> {
  const response = await fetch(`${BASE_API_URL}/folders`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(folderData),
  });

  if (!response.ok) {
    const message = await getErrorMessage(response, "Failed to create folder");
    throw new Error(message);
  }

  return await response.json();
}

export async function addSetToFolder(
  addSetToFolderData: AddSetToFolderData,
  token: string,
): Promise<Folder> {
  const response = await fetch(
    `${BASE_API_URL}/folders/${addSetToFolderData.folderId}/sets/${addSetToFolderData.flashcardSetId}`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    },
  );

  if (!response.ok) {
    const message = await getErrorMessage(
      response,
      "Failed to add set to folder",
    );
    throw new Error(message);
  }

  return await response.json();
}

export async function getMyFolders(token: string): Promise<Folder[]> {
  const response = await fetch(`${BASE_API_URL}/folders/me`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    const message = await getErrorMessage(response, "Failed to fetch folders");
    throw new Error(message);
  }

  return await response.json();
}

export async function getFoldersByNickname(
  nickname: string,
): Promise<Folder[]> {
  const response = await fetch(`${BASE_API_URL}/users/${nickname}/folders`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    const message = await getErrorMessage(response, "Failed to fetch folders");
    throw new Error(message);
  }

  return await response.json();
}
export async function getFolderDetailsById(
  folderId: number | string,
): Promise<Folder> {
  const response = await fetch(`${BASE_API_URL}/folders/${folderId}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    const message = await getErrorMessage(
      response,
      "Failed to fetch folder details",
    );
    throw new Error(message);
  }

  return await response.json();
}

export async function editFolder(
  folderData: EditFolderData,
  token: string,
): Promise<Folder> {
  const { folderId, name, description } = folderData;

  const response = await fetch(`${BASE_API_URL}/folders/${folderId}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({
      name,
      description,
    }),
  });

  if (!response.ok) {
    const message = await getErrorMessage(response, "Failed to edit folder");
    throw new Error(message);
  }

  return await response.json();
}

export async function deleteFolderById(
  folderId: number | string,
  token: string,
): Promise<void> {
  const response = await fetch(`${BASE_API_URL}/folders/${folderId}`, {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    const message = await getErrorMessage(response, "Failed to delete folder");
    throw new Error(message);
  }
}

export async function removeSetFromFolder(
  folderId: number | string,
  setId: number | string,
  token: string,
): Promise<void> {
  const response = await fetch(
    `${BASE_API_URL}/folders/${folderId}/sets/${setId}`,
    {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    },
  );

  if (!response.ok) {
    const message = await getErrorMessage(
      response,
      "Failed to remove set from folder",
    );
    throw new Error(message);
  }
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
