import type { GameMode } from "./gameModeTypes";

export interface GameResult {
  resultId: number;
  userId: number;
  setId: number;
  mode: GameMode;
  score: number;
  totalQuestions: number;
  durationSeconds?: number | null;
  completedAt: string;
}

export interface SaveGameResultRequest {
  setId: number;
  mode: GameMode;
  score: number;
  totalQuestions: number;
  durationSeconds?: number | null;
  questionResults?: SaveGameQuestionResultRequest[];
}

export interface SaveGameQuestionResultRequest {
  flashcardId: number;
  questionKey?: string | null;
  questionOrder: number;
  questionType: import("./gameModeTypes").GameQuestionType;
  answerWith?: import("./gameModeTypes").QuestionAnswerSide | null;
  prompt?: string | null;
  userAnswer?: string | null;
  correctAnswer?: string | null;
  wasCorrect: boolean;
  mistakesCount?: number | null;
}
