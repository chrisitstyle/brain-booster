import { apiRequest } from "@/api/apiClient";

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

export async function getCurrentUser(token: string): Promise<UserDTO> {
  return apiRequest<UserDTO>("/users/me", {
    token,
    cache: "no-store",
    fallbackMessage: "Failed to load user profile",
  });
}

export async function updateNickname(
  newNickname: string,
  token: string,
): Promise<UserDTO> {
  return apiRequest<UserDTO>("/profile/settings/nickname", {
    method: "PATCH",
    token,
    body: {
      newNickname,
    },
    fallbackMessage: "Failed to update nickname",
  });
}

export async function updateEmail(
  newEmail: string,
  token: string,
): Promise<UserEmailUpdateResponse> {
  return apiRequest<UserEmailUpdateResponse>("/profile/settings/email", {
    method: "PATCH",
    token,
    body: {
      newEmail,
    },
    fallbackMessage: "Failed to update email",
  });
}
