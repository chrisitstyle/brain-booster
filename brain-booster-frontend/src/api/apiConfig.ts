/**
 * Returns the base URL used for backend API requests.
 *
 * Server-side requests prefer `API_INTERNAL_URL` and fall back to
 * `NEXT_PUBLIC_API_URL`. Client-side requests use `NEXT_PUBLIC_API_URL`.
 *
 * @throws {Error} When no API base URL is configured.
 */
export function getApiBaseUrl(): string {
  const apiBaseUrl =
    typeof window === "undefined"
      ? (process.env.API_INTERNAL_URL ?? process.env.NEXT_PUBLIC_API_URL)
      : process.env.NEXT_PUBLIC_API_URL;

  if (!apiBaseUrl) {
    throw new Error("API base URL is not configured");
  }

  return apiBaseUrl;
}
