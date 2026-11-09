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

/**
 * Represents the user's FSRS scheduling preferences.
 */
export interface UserReviewSettings {
  desiredRetention: number;
  newCardsPerDay: number;
  reviewLimitPerDay: number;
  timezone: string;
}

/**
 * Represents a partial update of FSRS review preferences.
 *
 * Omitted fields retain their previously saved values.
 */
export type UpdateUserReviewSettingsRequest = Partial<UserReviewSettings>;

/**
 * Represents FSRS review activity on one local calendar day.
 */
export interface ReviewDailyActivity {
  date: string;
  reviews: number;
  newCards: number;
}

/**
 * Represents the number of submitted ratings of one type.
 */
export interface ReviewRatingCount {
  rating: ReviewRating;
  count: number;
}

/**
 * Represents scheduled reviews on one local calendar day.
 */
export interface ReviewDailyForecast {
  date: string;
  dueCards: number;
}

/**
 * Represents the authenticated user's FSRS learning dashboard.
 */
export interface ReviewDashboard {
  totalCards: number;
  newCards: number;
  learningCards: number;
  reviewCards: number;
  relearningCards: number;
  dueNow: number;
  reviewedToday: number;
  timezone: string;
  activity: ReviewDailyActivity[];
  ratings: ReviewRatingCount[];
  forecast: ReviewDailyForecast[];
}
