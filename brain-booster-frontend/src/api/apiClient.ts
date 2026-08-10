import { getApiBaseUrl } from "@/api/apiConfig";
import { ApiError } from "@/api/apiError";

type QueryParams = Record<string, string | number | boolean | null | undefined>;

type ResponseType = "json" | "text" | "void";

interface ApiRequestOptions extends Omit<RequestInit, "body"> {
  token?: string | null;
  body?: unknown;
  query?: QueryParams;
  fallbackMessage?: string;
  responseType?: ResponseType;
}

interface ErrorResponse {
  message?: string;
}

function buildQueryString(query?: QueryParams): string {
  if (!query) {
    return "";
  }

  const searchParams = new URLSearchParams();

  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined && value !== null && value !== "") {
      searchParams.set(key, String(value));
    }
  }

  const queryString = searchParams.toString();

  return queryString ? `?${queryString}` : "";
}

async function getErrorMessage(
  response: Response,
  fallbackMessage: string,
): Promise<string> {
  const text = await response.text();

  if (!text) {
    return fallbackMessage;
  }

  try {
    const error = JSON.parse(text) as ErrorResponse;

    return error.message?.trim() || fallbackMessage;
  } catch {
    return fallbackMessage;
  }
}

/**
 * Performs a request to the backend API.
 *
 * Handles API base URL resolution, query parameters, JSON request bodies,
 * authorization headers, response parsing, and API error normalization.
 *
 * Backend error messages are preferred when available. Otherwise,
 * the provided fallback message is used.
 *
 * @template T Expected response type.
 * @param path API endpoint path, for example `/flashcards`.
 * @param options Request configuration including authentication, body,
 * query parameters, fallback error message, and response type.
 * @returns Parsed API response.
 * @throws {ApiError} When the API responds with a non-successful status code.
 */
export async function apiRequest<T>(
  path: string,
  {
    token,
    body,
    query,
    fallbackMessage = "Request failed",
    responseType = "json",
    headers,
    ...requestInit
  }: ApiRequestOptions = {},
): Promise<T> {
  const requestHeaders = new Headers(headers);

  if (body !== undefined && !requestHeaders.has("Content-Type")) {
    requestHeaders.set("Content-Type", "application/json");
  }

  if (token) {
    requestHeaders.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(
    `${getApiBaseUrl()}${path}${buildQueryString(query)}`,
    {
      ...requestInit,
      headers: requestHeaders,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    },
  );

  if (!response.ok) {
    throw new ApiError(
      await getErrorMessage(response, fallbackMessage),
      response.status,
    );
  }

  if (responseType === "void" || response.status === 204) {
    return undefined as T;
  }

  if (responseType === "text") {
    return (await response.text()) as T;
  }

  return response.json() as Promise<T>;
}
