import { apiRequest } from "@/api/apiClient";

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
  return apiRequest<Folder>("/folders", {
    method: "POST",
    token,
    body: folderData,
    fallbackMessage: "Failed to create folder",
  });
}

export async function addSetToFolder(
  addSetToFolderData: AddSetToFolderData,
  token: string,
): Promise<Folder> {
  return apiRequest<Folder>(
    `/folders/${addSetToFolderData.folderId}/sets/${addSetToFolderData.flashcardSetId}`,
    {
      method: "POST",
      token,
      fallbackMessage: "Failed to add set to folder",
    },
  );
}

export async function getMyFolders(token: string): Promise<Folder[]> {
  return apiRequest<Folder[]>("/folders/me", {
    token,
    fallbackMessage: "Failed to fetch folders",
  });
}

export async function getFoldersByNickname(
  nickname: string,
): Promise<Folder[]> {
  return apiRequest<Folder[]>(`/users/${nickname}/folders`, {
    fallbackMessage: "Failed to fetch folders",
  });
}

export async function getFolderDetailsById(
  folderId: number | string,
): Promise<Folder> {
  return apiRequest<Folder>(`/folders/${folderId}`, {
    fallbackMessage: "Failed to fetch folder details",
  });
}

export async function editFolder(
  folderData: EditFolderData,
  token: string,
): Promise<Folder> {
  const { folderId, name, description } = folderData;

  return apiRequest<Folder>(`/folders/${folderId}`, {
    method: "PATCH",
    token,
    body: {
      name,
      description,
    },
    fallbackMessage: "Failed to edit folder",
  });
}

export async function deleteFolderById(
  folderId: number | string,
  token: string,
): Promise<void> {
  return apiRequest<void>(`/folders/${folderId}`, {
    method: "DELETE",
    token,
    responseType: "void",
    fallbackMessage: "Failed to delete folder",
  });
}

export async function removeSetFromFolder(
  folderId: number | string,
  setId: number | string,
  token: string,
): Promise<void> {
  return apiRequest<void>(`/folders/${folderId}/sets/${setId}`, {
    method: "DELETE",
    token,
    responseType: "void",
    fallbackMessage: "Failed to remove set from folder",
  });
}
