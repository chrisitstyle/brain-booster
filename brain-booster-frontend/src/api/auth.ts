import { apiRequest } from "@/api/apiClient";

interface RegisterUserData {
  nickname: string;
  email: string;
  password: string;
}

interface AuthenticateUserData {
  email: string;
  password: string;
}

interface AuthenticateUserResponse {
  token: string;
}

export async function registerUser(data: RegisterUserData): Promise<string> {
  return apiRequest<string>("/auth/register", {
    method: "POST",
    body: data,
    responseType: "text",
    fallbackMessage: "Registration failed",
  });
}

export async function authenticateUser(
  data: AuthenticateUserData,
): Promise<string> {
  const response = await apiRequest<AuthenticateUserResponse>(
    "/auth/authenticate",
    {
      method: "POST",
      body: data,
      fallbackMessage: "Login failed",
    },
  );

  return response.token;
}
