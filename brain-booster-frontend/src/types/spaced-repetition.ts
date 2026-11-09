/**
 * Represents the user's recall rating for a flashcard.
 */
export type ReviewRating = "AGAIN" | "HARD" | "GOOD" | "EASY";

/**
 * Represents the current FSRS learning stage.
 */
export type ReviewStage = "NEW" | "LEARNING" | "REVIEW" | "RELEARNING";

/**
 * Represents a flashcard available for review.
 */
export interface ReviewCard {
  flashcardId: number;
  term: string;
  definition: string;
  stage: ReviewStage;
  dueAt: string | null;
}

/**
 * Represents one logical review submission.
 *
 * Retries must reuse the original requestId.
 */
export interface SubmitReviewRequest {
  flashcardId: number;
  rating: ReviewRating;
  requestId: string;
}

/**
 * Represents a successfully recorded review.
 */
export interface ReviewResult {
  requestId: string;
  flashcardId: number;
  rating: ReviewRating;
  stage: ReviewStage;
  reviewedAt: string;
  nextReviewAt: string;
}
