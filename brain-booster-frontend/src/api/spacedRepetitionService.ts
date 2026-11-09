import { apiRequest } from "@/api/apiClient";

import type {
  ReviewCard,
  ReviewResult,
  SubmitReviewRequest,
  UserReviewSettings,
  UpdateUserReviewSettingsRequest,
} from "@/types/spaced-repetition";

/**
 * Fetches the flashcards currently available for FSRS review.
 *
 * @param flashcardSetId Identifier of the selected flashcard set.
 * @param token Authenticated user's JWT.
 * @param signal Optional cancellation signal for stale requests.
 * @returns Due and new flashcards in the order provided by the backend.
 */
export async function getReviewQueue(
  flashcardSetId: number,
  token: string,
  signal?: AbortSignal,
): Promise<ReviewCard[]> {
  return apiRequest<ReviewCard[]>(`/reviews/sets/${flashcardSetId}/queue`, {
    token,
    signal,
    cache: "no-store",
    fallbackMessage: "Failed to load flashcard reviews.",
  });
}

/**
 * Records a recall rating and returns the updated FSRS schedule.
 *
 * The requestId is supplied by the caller so failed network requests
 * can be retried without applying the same rating twice.
 *
 * @param reviewRequest Review rating and unique request identifier.
 * @param token Authenticated user's JWT.
 * @returns Persisted review and its next scheduled date.
 */
export async function submitFlashcardReview(
  reviewRequest: SubmitReviewRequest,
  token: string,
): Promise<ReviewResult> {
  return apiRequest<ReviewResult>("/reviews", {
    method: "POST",
    token,
    body: reviewRequest,
    fallbackMessage: "Failed to save flashcard review.",
  });
}

/**
 * Fetches the authenticated user's effective FSRS settings.
 *
 * The backend returns default values when no settings are saved.
 */
export async function getReviewSettings(
  token: string,
  signal?: AbortSignal,
): Promise<UserReviewSettings> {
  return apiRequest<UserReviewSettings>("/reviews/settings", {
    token,
    signal,
    cache: "no-store",
    fallbackMessage: "Failed to load review settings.",
  });
}

/**
 * Applies a partial update to the authenticated user's FSRS settings.
 */
export async function updateReviewSettings(
  settingsUpdate: UpdateUserReviewSettingsRequest,
  token: string,
): Promise<UserReviewSettings> {
  return apiRequest<UserReviewSettings>("/reviews/settings", {
    method: "PATCH",
    token,
    body: settingsUpdate,
    fallbackMessage: "Failed to update review settings.",
  });
}
