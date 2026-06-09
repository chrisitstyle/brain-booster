package com.brainbooster.gameresult;

import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.gameresult.dto.GameResultDTO;
import com.brainbooster.gameresult.mapper.GameResultMapper;
import com.brainbooster.user.User;
import com.brainbooster.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class GameResultService {

    private final GameResultRepository gameResultRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final GameResultMapper gameResultMapper;

    @Transactional
    public GameResultDTO saveGameResult(SaveGameResultRequest request) {
        User authUser = SecurityUtils.getAuthenticatedUser();

        validateScore(request.score(), request.totalQuestions());

        FlashcardSet flashcardSet = flashcardSetRepository.findById(request.setId())
                .orElseThrow(() -> new NoSuchElementException(
                        "FlashcardSet with id: " + request.setId() + " not found"
                ));

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
        gameResult.setCompletedAt(LocalDateTime.now());

        GameResult savedGameResult = gameResultRepository.save(gameResult);

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

    private void validateScore(Integer score, Integer totalQuestions) {
        if (score > totalQuestions) {
            throw new IllegalArgumentException("Score cannot be greater than total questions.");
        }
    }
}