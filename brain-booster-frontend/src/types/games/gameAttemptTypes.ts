import type {
  GameMode,
  GameQuestionType,
  QuestionAnswerSide,
} from "./gameModeTypes";

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  numberOfElements: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface GameAttemptFilters {
  page?: number;
  size?: number;
  setId?: number;
  mode?: GameMode;
  from?: string;
  to?: string;
}

export interface GameAttempt {
  attemptId: number;
  userId: number;
  setId: number;
  mode: GameMode;
  score: number;
  totalQuestions: number;
  durationSeconds?: number | null;
  completedAt: string;
  questionResults: GameQuestionResult[];
}

export interface GameQuestionResult {
  questionResultId: number;
  attemptId: number;
  flashcardId: number;
  questionKey?: string | null;
  questionOrder: number;
  questionType: GameQuestionType;
  answerWith?: QuestionAnswerSide | null;
  prompt?: string | null;
  userAnswer?: string | null;
  correctAnswer?: string | null;
  wasCorrect: boolean;
  mistakesCount: number;
  answeredAt: string;
}
