package com.brainbooster.gameresult;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.gameresult.attempt.GameAttempt;
import com.brainbooster.gameresult.attempt.GameAttemptRepository;
import com.brainbooster.gameresult.dto.GameResultDTO;
import com.brainbooster.gameresult.dto.SaveGameQuestionResultRequest;
import com.brainbooster.gameresult.dto.SaveGameResultRequest;
import com.brainbooster.gameresult.mapper.GameResultMapper;
import com.brainbooster.gameresult.questionresult.GameQuestionResult;
import com.brainbooster.user.User;
import com.brainbooster.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameResultService {

    private static final String NOT_FOUND_MSG_SUFFIX = " not found";
    private static final String FLASHCARD_SET_WITH_ID_MSG_PREFIX = "FlashcardSet with id: ";
    private static final String GAME_RESULT_WITH_ID_MSG_PREFIX = "GameResult with id: ";
    private static final String FLASHCARD_WITH_ID_MSG_PREFIX = "Flashcard with id: ";
    private static final String ACCESS_GAME_RESULT_DENIED_MSG =
            "You are not allowed to access this game result.";
    private static final String DELETE_GAME_RESULT_DENIED_MSG =
            "You are not allowed to delete this game result.";
    private static final String SCORE_GREATER_THAN_TOTAL_QUESTIONS_MSG =
            "Score cannot be greater than total questions.";

    private final GameResultRepository gameResultRepository;
    private final GameAttemptRepository gameAttemptRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final FlashcardRepository flashcardRepository;
    private final GameResultMapper gameResultMapper;

    @Transactional
    public GameResultDTO saveGameResult(SaveGameResultRequest request) {
        User authUser = SecurityUtils.getAuthenticatedUser();

        validateScore(request.score(), request.totalQuestions());

        FlashcardSet flashcardSet = flashcardSetRepository.findById(request.setId())
                .orElseThrow(() -> new NoSuchElementException(
                        buildFlashcardSetNotFoundMessage(request.setId())
                ));

        Instant completedAt = Instant.now();

        GameResult gameResult = gameResultRepository
                .findByUser_UserIdAndSet_SetIdAndMode(
                        authUser.getUserId(),
                        request.setId(),
                        request.mode()
                )
                .orElseGet(() -> GameResult.builder()
                        .user(authUser)
                        .set(flashcardSet)
                        .mode(request.mode())
                        .build()
                );

        gameResult.setScore(request.score());
        gameResult.setTotalQuestions(request.totalQuestions());
        gameResult.setDurationSeconds(request.durationSeconds());
        gameResult.setCompletedAt(completedAt);

        GameResult savedGameResult = gameResultRepository.save(gameResult);

        saveGameAttempt(
                authUser,
                flashcardSet,
                request,
                completedAt
        );

        return gameResultMapper.toDto(savedGameResult);
    }

    @Transactional(readOnly = true)
    public List<GameResultDTO> getMyGameResults(Long setId) {
        User authUser = SecurityUtils.getAuthenticatedUser();

        List<GameResult> gameResults = setId == null
                ? gameResultRepository.findByUser_UserIdOrderByCompletedAtDesc(
                authUser.getUserId()
        )
                : gameResultRepository.findByUser_UserIdAndSet_SetIdOrderByCompletedAtDesc(
                authUser.getUserId(),
                setId
        );

        return gameResults.stream()
                .map(gameResultMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GameResultDTO> getAllGameResults(Long setId) {
        SecurityUtils.verifyAdmin();

        List<GameResult> gameResults = setId == null
                ? gameResultRepository.findAllByOrderByCompletedAtDesc()
                : gameResultRepository.findBySet_SetIdOrderByCompletedAtDesc(setId);

        return gameResults.stream()
                .map(gameResultMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public GameResultDTO getGameResultById(Long resultId) {
        GameResult gameResult = getAccessibleGameResult(
                resultId,
                ACCESS_GAME_RESULT_DENIED_MSG
        );

        return gameResultMapper.toDto(gameResult);
    }

    @Transactional
    public void deleteGameResult(Long resultId) {
        GameResult gameResult = getAccessibleGameResult(
                resultId,
                DELETE_GAME_RESULT_DENIED_MSG
        );

        gameResultRepository.delete(gameResult);
    }

    private void saveGameAttempt(
            User authUser,
            FlashcardSet flashcardSet,
            SaveGameResultRequest request,
            Instant completedAt
    ) {
        GameAttempt attempt = GameAttempt.builder()
                .user(authUser)
                .set(flashcardSet)
                .mode(request.mode())
                .score(request.score())
                .totalQuestions(request.totalQuestions())
                .durationSeconds(request.durationSeconds())
                .completedAt(completedAt)
                .build();

        List<SaveGameQuestionResultRequest> questionResults =
                request.questionResults() == null ? List.of() : request.questionResults();

        if (!questionResults.isEmpty()) {
            Map<Long, Flashcard> flashcardsById = getFlashcardsById(
                    questionResults,
                    request.setId()
            );

            questionResults.forEach(questionResult ->
                    attempt.addQuestionResult(createQuestionResult(
                            questionResult,
                            flashcardsById.get(questionResult.flashcardId()),
                            completedAt
                    ))
            );
        }

        gameAttemptRepository.save(attempt);
    }

    private Map<Long, Flashcard> getFlashcardsById(
            List<SaveGameQuestionResultRequest> questionResults,
            Long setId
    ) {
        Set<Long> flashcardIds = questionResults.stream()
                .map(SaveGameQuestionResultRequest::flashcardId)
                .collect(Collectors.toSet());

        Map<Long, Flashcard> flashcardsById = flashcardRepository.findAllById(flashcardIds)
                .stream()
                .collect(Collectors.toMap(
                        Flashcard::getFlashcardId,
                        Function.identity()
                ));

        flashcardIds.stream()
                .filter(flashcardId -> !flashcardsById.containsKey(flashcardId))
                .findFirst()
                .ifPresent(flashcardId -> {
                    throw new NoSuchElementException(
                            buildFlashcardNotFoundMessage(flashcardId)
                    );
                });

        flashcardsById.values().forEach(flashcard -> {
            Long flashcardSetId = flashcard.getFlashcardSet().getSetId();

            if (!setId.equals(flashcardSetId)) {
                throw new IllegalArgumentException(
                        FLASHCARD_WITH_ID_MSG_PREFIX + flashcard.getFlashcardId()
                                + " does not belong to set: " + setId
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
                .mistakesCount(request.mistakesCount() == null ? 0 : request.mistakesCount())
                .answeredAt(answeredAt)
                .build();
    }

    private void validateScore(Integer score, Integer totalQuestions) {
        if (score > totalQuestions) {
            throw new IllegalArgumentException(SCORE_GREATER_THAN_TOTAL_QUESTIONS_MSG);
        }
    }

    private GameResult getAccessibleGameResult(Long resultId, String accessDeniedMessage) {
        User authUser = SecurityUtils.getAuthenticatedUser();
        GameResult gameResult = findGameResultById(resultId);

        verifyGameResultAccess(gameResult, authUser, accessDeniedMessage);

        return gameResult;
    }

    private GameResult findGameResultById(Long resultId) {
        return gameResultRepository.findById(resultId)
                .orElseThrow(() -> new NoSuchElementException(
                        buildGameResultNotFoundMessage(resultId)
                ));
    }

    private void verifyGameResultAccess(
            GameResult gameResult,
            User authUser,
            String accessDeniedMessage
    ) {
        boolean isAdmin = SecurityUtils.isAdmin(authUser);
        boolean isOwner = gameResult.getUser().getUserId().equals(authUser.getUserId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(accessDeniedMessage);
        }
    }

    private String buildFlashcardSetNotFoundMessage(Long setId) {
        return FLASHCARD_SET_WITH_ID_MSG_PREFIX + setId + NOT_FOUND_MSG_SUFFIX;
    }

    private String buildGameResultNotFoundMessage(Long resultId) {
        return GAME_RESULT_WITH_ID_MSG_PREFIX + resultId + NOT_FOUND_MSG_SUFFIX;
    }

    private String buildFlashcardNotFoundMessage(Long flashcardId) {
        return FLASHCARD_WITH_ID_MSG_PREFIX + flashcardId + NOT_FOUND_MSG_SUFFIX;
    }
}