import type { Flashcard } from "@/api/flashcardService";
import type { SaveGameQuestionResultRequest } from "@/types/games";
import type {
  GameQuestionType,
  QuestionAnswerSide,
} from "@/types/games/gameModeTypes";

interface CreateQuestionResultParams {
  flashcardId: number;
  questionKey: string;
  questionOrder: number;
  questionType: GameQuestionType;
  answerWith?: QuestionAnswerSide;
  prompt?: string;
  userAnswer?: string;
  correctAnswer?: string;
  wasCorrect: boolean;
  mistakesCount?: number;
}

export function createQuestionResult({
  flashcardId,
  questionKey,
  questionOrder,
  questionType,
  answerWith,
  prompt,
  userAnswer,
  correctAnswer,
  wasCorrect,
  mistakesCount = wasCorrect ? 0 : 1,
}: CreateQuestionResultParams): SaveGameQuestionResultRequest {
  return {
    flashcardId,
    questionKey,
    questionOrder,
    questionType,
    ...(answerWith !== undefined ? { answerWith } : {}),
    ...(prompt !== undefined ? { prompt } : {}),
    ...(userAnswer !== undefined ? { userAnswer } : {}),
    ...(correctAnswer !== undefined ? { correctAnswer } : {}),
    wasCorrect,
    mistakesCount,
  };
}

export function createDefinitionAnswerQuestionResult({
  flashcard,
  questionOrder,
  questionType,
  userAnswer,
  wasCorrect,
  mistakesCount,
}: {
  flashcard: Flashcard;
  questionOrder: number;
  questionType: Exclude<GameQuestionType, "matching" | "true-false">;
  userAnswer: string;
  wasCorrect: boolean;
  mistakesCount?: number;
}): SaveGameQuestionResultRequest {
  return createQuestionResult({
    flashcardId: flashcard.flashcardId,
    questionKey: `${questionType}-${flashcard.flashcardId}-${questionOrder}`,
    questionOrder,
    questionType,
    answerWith: "definition",
    prompt: flashcard.term,
    userAnswer,
    correctAnswer: flashcard.definition,
    wasCorrect,
    mistakesCount,
  });
}

export function createTrueFalseQuestionResult({
  flashcard,
  questionOrder,
  userAnswer,
  correctAnswer,
  wasCorrect,
}: {
  flashcard: Flashcard;
  questionOrder: number;
  userAnswer: string;
  correctAnswer: string;
  wasCorrect: boolean;
}): SaveGameQuestionResultRequest {
  return createQuestionResult({
    flashcardId: flashcard.flashcardId,
    questionKey: `true-false-${flashcard.flashcardId}-${questionOrder}`,
    questionOrder,
    questionType: "true-false",
    answerWith: "definition",
    prompt: flashcard.term,
    userAnswer,
    correctAnswer,
    wasCorrect,
  });
}

export function createMatchingQuestionResults({
  cards,
  mistakesByFlashcardId,
}: {
  cards: Flashcard[];
  mistakesByFlashcardId: Record<number, number>;
}): SaveGameQuestionResultRequest[] {
  return cards.map((card, index) => {
    const mistakesCount = mistakesByFlashcardId[card.flashcardId] ?? 0;

    return createQuestionResult({
      flashcardId: card.flashcardId,
      questionKey: `matching-${card.flashcardId}-${index}`,
      questionOrder: index,
      questionType: "matching",
      answerWith: "definition",
      prompt: card.term,
      correctAnswer: card.definition,
      wasCorrect: mistakesCount === 0,
      mistakesCount,
    });
  });
}

export function mapCustomTestQuestionType(type: string): GameQuestionType {
  if (type === "multipleChoice") return "multiple-choice";
  if (type === "trueFalse") return "true-false";
  if (type === "matching") return "matching";

  return "written";
}
