/**
 * Represents an HTTP error returned by the backend API.
 *
 * Keeps both the user-facing error message and the HTTP status code,
 * allowing callers to handle specific response statuses when needed.
 */
export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
  ) {
    super(message);
    this.name = "ApiError";
  }
}
