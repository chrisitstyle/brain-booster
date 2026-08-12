package com.brainbooster.gameresult.attempt;

import com.brainbooster.exception.ResourceNotFoundException;
import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.gameresult.dto.SaveGameQuestionResultRequest;
import com.brainbooster.gameresult.dto.SaveGameResultRequest;
import com.brainbooster.gameresult.questionresult.GameQuestionResult;
import com.brainbooster.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameAttemptRecorder {

    private static final String NOT_FOUND_MSG_SUFFIX = " not found";
    private static final String FLASHCARD_WITH_ID_MSG_PREFIX = "Flashcard with id: ";

    private final GameAttemptRepository gameAttemptRepository;
    private final FlashcardRepository flashcardRepository;

    public void recordAttempt(
            User user,
            FlashcardSet flashcardSet,
            SaveGameResultRequest request,
            Instant completedAt
    ) {
        GameAttempt attempt = GameAttempt.builder()
                .user(user)
                .set(flashcardSet)
                .mode(request.mode())
                .score(request.score())
                .totalQuestions(request.totalQuestions())
                .durationSeconds(request.durationSeconds())
                .completedAt(completedAt)
                .build();

        List<SaveGameQuestionResultRequest> questionResults = request.questionResults() == null
                        ? List.of()
                        : request.questionResults();

        if (!questionResults.isEmpty()) {
            addQuestionResults(
                    attempt,
                    questionResults,
                    flashcardSet.getSetId(),
                    completedAt
            );
        }

        gameAttemptRepository.save(attempt);
    }

    private void addQuestionResults(
            GameAttempt attempt,
            List<SaveGameQuestionResultRequest> questionResults,
            Long setId,
            Instant answeredAt
    ) {
        Map<Long, Flashcard> flashcardsById = getFlashcardsById(questionResults, setId);

        questionResults.forEach(questionResult ->
                attempt.addQuestionResult(
                        createQuestionResult(
                                questionResult,
                                flashcardsById.get(questionResult.flashcardId()),
                                answeredAt
                        )
                )
        );
    }

    private Map<Long, Flashcard> getFlashcardsById(
            List<SaveGameQuestionResultRequest> questionResults,
            Long setId
    ) {
        Set<Long> flashcardIds = questionResults.stream()
                .map(SaveGameQuestionResultRequest::flashcardId)
                .collect(Collectors.toSet());

        Map<Long, Flashcard> flashcardsById =
                flashcardRepository.findAllById(flashcardIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Flashcard::getFlashcardId,
                                Function.identity()
                        ));

        flashcardIds.stream()
                .filter(flashcardId -> !flashcardsById.containsKey(flashcardId))
                .findFirst()
                .ifPresent(flashcardId -> {
                    throw new ResourceNotFoundException(
                            buildFlashcardNotFoundMessage(flashcardId));
                });

        flashcardsById.values().forEach(flashcard -> {
            Long flashcardSetId = flashcard.getFlashcardSet().getSetId();

            if (!setId.equals(flashcardSetId)) {
                throw new IllegalArgumentException(
                        FLASHCARD_WITH_ID_MSG_PREFIX
                                + flashcard.getFlashcardId()
                                + " does not belong to set: "
                                + setId
                );
            }
        });

        return flashcardsById;
    }

    private GameQuestionResult createQuestionResult(
            SaveGameQuestionResultRequest request,
            Flashcard flashcard,
            Instant answeredAt
    ) {
        return GameQuestionResult.builder()
                .flashcard(flashcard)
                .questionKey(request.questionKey())
                .questionOrder(request.questionOrder())
                .questionType(request.questionType())
                .answerWith(request.answerWith())
                .prompt(request.prompt())
                .userAnswer(request.userAnswer())
                .correctAnswer(request.correctAnswer())
                .wasCorrect(request.wasCorrect())
                .mistakesCount(
                        request.mistakesCount() == null
                                ? 0
                                : request.mistakesCount()
                )
                .answeredAt(answeredAt)
                .build();
    }

    private String buildFlashcardNotFoundMessage(Long flashcardId) {
        return FLASHCARD_WITH_ID_MSG_PREFIX
                + flashcardId
                + NOT_FOUND_MSG_SUFFIX;
    }
}
