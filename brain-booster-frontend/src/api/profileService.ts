import { getApiBaseUrl } from "@/api/apiConfig";

export interface UserDTO {
  userId: number;
  nickname: string;
  email: string;
  role: "USER" | "ADMIN";
  createdAt: string;
}

export interface UserEmailUpdateResponse {
  email: string;
  token: string;
}

interface ErrorResponse {
  message?: string;
}

async function getErrorMessage(
  response: Response,
  fallbackMessage: string,
): Promise<string> {
  const errorData = (await response
    .json()
    .catch(() => null)) as ErrorResponse | null;

  return errorData?.message ?? fallbackMessage;
}

export async function getCurrentUser(token: string): Promise<UserDTO> {
  const response = await fetch(`${getApiBaseUrl()}/users/me`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Failed to load user profile"),
    );
  }

  return response.json();
}

export async function updateNickname(
  newNickname: string,
  token: string,
): Promise<UserDTO> {
  const response = await fetch(`${getApiBaseUrl()}/profile/settings/nickname`, {
    method: "PATCH",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      newNickname,
    }),
  });

  if (!response.ok) {
    throw new Error(
      await getErrorMessage(response, "Failed to update nickname"),
    );
  }

  return response.json();
}

export async function updateEmail(
  newEmail: string,
  token: string,
): Promise<UserEmailUpdateResponse> {
  const response = await fetch(`${getApiBaseUrl()}/profile/settings/email`, {
    method: "PATCH",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      newEmail,
    }),
  });

  if (!response.ok) {
    throw new Error(await getErrorMessage(response, "Failed to update email"));
  }

  return response.json();
}
