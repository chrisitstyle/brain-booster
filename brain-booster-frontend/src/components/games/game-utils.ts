import type { Flashcard } from "@/api/flashcardService";

export function shuffleArray<T>(items: T[]): T[] {
  const copy = [...items];

  for (let i = copy.length - 1; i > 0; i -= 1) {
    const randomIndex = Math.floor(Math.random() * (i + 1));
    [copy[i], copy[randomIndex]] = [copy[randomIndex], copy[i]];
  }

  return copy;
}

export interface QuizQuestion {
  flashcardId: number;
  prompt: string;
  correctAnswer: string;
  options: string[];
}

export function buildQuizQuestions(
  flashcards: Flashcard[],
  optionsCount = 4,
): QuizQuestion[] {
  const definitions = Array.from(
    new Set(flashcards.map((card) => card.definition).filter(Boolean)),
  );

  return shuffleArray(flashcards).map((card) => {
    const wrongAnswers = shuffleArray(
      definitions.filter((definition) => definition !== card.definition),
    ).slice(0, optionsCount - 1);

    return {
      flashcardId: card.flashcardId,
      prompt: card.term,
      correctAnswer: card.definition,
      options: shuffleArray([card.definition, ...wrongAnswers]),
    };
  });
}
