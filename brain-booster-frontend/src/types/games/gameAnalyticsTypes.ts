import type { GameMode, GameQuestionType } from "./gameModeTypes";

export interface GameAnalyticsSummary {
  totalAttempts: number;
  averageScore: number;
  bestScore: number;
  averageDuration: number;
  lastAttemptAt: string | null;
  accuracyPercentage: number;
}

export interface GameProgressPoint {
  attemptId: number;
  completedAt: string;
  score: number;
  totalQuestions: number;
  percentage: number;
  durationSeconds?: number | null;
  mode: GameMode;
}

export interface WeakFlashcard {
  flashcardId: number;
  term: string;
  definition: string;
  totalAnswers: number;
  correctAnswers: number;
  incorrectAnswers: number;
  totalMistakes: number;
  accuracyPercentage: number;
  lastAnsweredAt: string | null;
}

export interface QuestionTypeAnalytics {
  questionType: GameQuestionType;
  totalAnswers: number;
  correctAnswers: number;
  incorrectAnswers: number;
  accuracyPercentage: number;
}
