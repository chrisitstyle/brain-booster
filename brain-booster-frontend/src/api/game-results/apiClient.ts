import { getApiBaseUrl } from "../apiConfig";

const BASE_API_URL = getApiBaseUrl();
export function createAuthHeaders(token: string): HeadersInit {
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  };
}

export function buildQueryString(
  params: Record<string, string | number | undefined | null>,
) {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      searchParams.append(key, String(value));
    }
  });

  const queryString = searchParams.toString();

  return queryString ? `?${queryString}` : "";
}

export function getApiUrl(path: string) {
  if (!BASE_API_URL) {
    throw new Error("NEXT_PUBLIC_API_URL is not defined");
  }

  return `${BASE_API_URL}${path}`;
}

export async function fetchJson<T>(
  url: string,
  options: RequestInit,
): Promise<T> {
  const response = await fetch(url, options);

  if (!response.ok) {
    const errorMessage = await response.text();
    throw new Error(errorMessage || "API request failed");
  }

  return response.json() as Promise<T>;
}

export async function fetchWithoutBody(
  url: string,
  options: RequestInit,
): Promise<void> {
  const response = await fetch(url, options);

  if (!response.ok) {
    const errorMessage = await response.text();
    throw new Error(errorMessage || "API request failed");
  }
}
