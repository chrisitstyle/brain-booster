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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameResultService {

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
                        "FlashcardSet with id: " + request.setId() + " not found"
                ));

        LocalDateTime completedAt = LocalDateTime.now();

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
        User authUser = SecurityUtils.getAuthenticatedUser();

        GameResult gameResult = gameResultRepository.findById(resultId)
                .orElseThrow(() -> new NoSuchElementException(
                        "GameResult with id: " + resultId + " not found"
                ));

        boolean isAdmin = SecurityUtils.isAdmin(authUser);
        boolean isOwner = gameResult.getUser().getUserId().equals(authUser.getUserId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not allowed to access this game result.");
        }

        return gameResultMapper.toDto(gameResult);
    }

    @Transactional
    public void deleteGameResult(Long resultId) {
        User authUser = SecurityUtils.getAuthenticatedUser();

        GameResult gameResult = gameResultRepository.findById(resultId)
                .orElseThrow(() -> new NoSuchElementException(
                        "GameResult with id: " + resultId + " not found"
                ));

        boolean isAdmin = SecurityUtils.isAdmin(authUser);
        boolean isOwner = gameResult.getUser().getUserId().equals(authUser.getUserId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not allowed to delete this game result.");
        }

        gameResultRepository.delete(gameResult);
    }

    private void saveGameAttempt(
            User authUser,
            FlashcardSet flashcardSet,
            SaveGameResultRequest request,
            LocalDateTime completedAt
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
                            "Flashcard with id: " + flashcardId + " not found"
                    );
                });

        flashcardsById.values().forEach(flashcard -> {
            Long flashcardSetId = flashcard.getFlashcardSet().getSetId();

            if (!setId.equals(flashcardSetId)) {
                throw new IllegalArgumentException(
                        "Flashcard with id: " + flashcard.getFlashcardId()
                                + " does not belong to set: " + setId
                );
            }
        });

        return flashcardsById;
    }

    private GameQuestionResult createQuestionResult(
            SaveGameQuestionResultRequest request,
            Flashcard flashcard,
            LocalDateTime answeredAt
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
            throw new IllegalArgumentException("Score cannot be greater than total questions.");
        }
    }
}