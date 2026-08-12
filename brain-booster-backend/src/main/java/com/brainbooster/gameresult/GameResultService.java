package com.brainbooster.gameresult;

import com.brainbooster.exception.ResourceNotFoundException;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.gameresult.attempt.GameAttemptRecorder;
import com.brainbooster.gameresult.dto.GameResultDTO;
import com.brainbooster.gameresult.dto.SaveGameResultRequest;
import com.brainbooster.gameresult.mapper.GameResultMapper;
import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.security.CurrentUserProvider;
import com.brainbooster.security.authorization.AdminPolicy;
import com.brainbooster.security.authorization.OwnerOrAdminPolicy;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameResultService {

    private static final String NOT_FOUND_MSG_SUFFIX = " not found";
    private static final String FLASHCARD_SET_WITH_ID_MSG_PREFIX = "FlashcardSet with id: ";
    private static final String GAME_RESULT_WITH_ID_MSG_PREFIX = "GameResult with id: ";
    private static final String ACCESS_GAME_RESULT_DENIED_MSG = "You are not allowed to access this game result.";
    private static final String DELETE_GAME_RESULT_DENIED_MSG = "You are not allowed to delete this game result.";
    private static final String SCORE_GREATER_THAN_TOTAL_QUESTIONS_MSG = "Score cannot be greater than total questions.";

    private final GameResultRepository gameResultRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final GameAttemptRecorder gameAttemptRecorder;
    private final GameResultMapper gameResultMapper;
    private final CurrentUserProvider currentUserProvider;
    private final OwnerOrAdminPolicy ownerOrAdminPolicy;
    private final AdminPolicy adminPolicy;
    private final UserRepository userRepository;

    @Transactional
    public GameResultDTO saveGameResult(SaveGameResultRequest request) {
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();

        User userReference = userRepository.getReferenceById(authenticatedUser.userId());

        validateScore(request.score(), request.totalQuestions());

        FlashcardSet flashcardSet = flashcardSetRepository.findById(request.setId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        buildFlashcardSetNotFoundMessage(request.setId())
                ));

        Instant completedAt = Instant.now();

        GameResult gameResult = gameResultRepository
                .findByUser_UserIdAndSet_SetIdAndMode(
                        authenticatedUser.userId(),
                        request.setId(),
                        request.mode())
                .orElseGet(() -> GameResult.builder()
                        .user(userReference)
                        .set(flashcardSet)
                        .mode(request.mode())
                        .build());

        gameResult.setScore(request.score());
        gameResult.setTotalQuestions(request.totalQuestions());
        gameResult.setDurationSeconds(request.durationSeconds());
        gameResult.setCompletedAt(completedAt);

        GameResult savedGameResult = gameResultRepository.save(gameResult);

        gameAttemptRecorder.recordAttempt(
                userReference,
                flashcardSet,
                request,
                completedAt);

        return gameResultMapper.toDto(savedGameResult);
    }

    @Transactional(readOnly = true)
    public List<GameResultDTO> getMyGameResults(Long setId) {
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();

        List<GameResult> gameResults = setId == null
                ? gameResultRepository.findByUser_UserIdOrderByCompletedAtDesc(
               authenticatedUser.userId()
        )
                : gameResultRepository.findByUser_UserIdAndSet_SetIdOrderByCompletedAtDesc(
                authenticatedUser.userId(),
                setId);

        return gameResults.stream()
                .map(gameResultMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GameResultDTO> getAllGameResults(Long setId) {
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();
        adminPolicy.verify(authenticatedUser);

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
                ACCESS_GAME_RESULT_DENIED_MSG);

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

    private void validateScore(Integer score, Integer totalQuestions) {
        if (score > totalQuestions) {
            throw new IllegalArgumentException(
                    SCORE_GREATER_THAN_TOTAL_QUESTIONS_MSG
            );
        }
    }

    private GameResult getAccessibleGameResult(
            Long resultId,
            String accessDeniedMessage
    ) {
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();
        GameResult gameResult = findGameResultById(resultId);

        verifyGameResultAccess(
                gameResult,
                authenticatedUser,
                accessDeniedMessage
        );

        return gameResult;
    }

    private GameResult findGameResultById(Long resultId) {
        return gameResultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        buildGameResultNotFoundMessage(resultId)
                ));
    }

    private void verifyGameResultAccess(
            GameResult gameResult,
            AuthenticatedUser authenticatedUser,
            String accessDeniedMessage) {
        ownerOrAdminPolicy.verify(
                authenticatedUser,
                gameResult.getUser().getUserId(),
                accessDeniedMessage);
    }

    private String buildFlashcardSetNotFoundMessage(Long setId) {
        return FLASHCARD_SET_WITH_ID_MSG_PREFIX
                + setId
                + NOT_FOUND_MSG_SUFFIX;
    }

    private String buildGameResultNotFoundMessage(Long resultId) {
        return GAME_RESULT_WITH_ID_MSG_PREFIX
                + resultId
                + NOT_FOUND_MSG_SUFFIX;
    }
}