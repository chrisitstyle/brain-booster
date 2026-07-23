package com.brainbooster.gameresult.attempt;

import com.brainbooster.gameresult.GameMode;
import com.brainbooster.gameresult.dto.GameAttemptDTO;
import com.brainbooster.gameresult.dto.GameAttemptSummaryDTO;
import com.brainbooster.gameresult.dto.GameQuestionResultDTO;
import com.brainbooster.gameresult.mapper.GameAttemptMapper;
import com.brainbooster.gameresult.mapper.GameQuestionResultMapper;
import com.brainbooster.gameresult.questionresult.GameQuestionResultRepository;
import com.brainbooster.user.User;
import com.brainbooster.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameAttemptService {

    private static final ZoneOffset DATE_FILTER_ZONE_OFFSET = ZoneOffset.UTC;
    private static final String GAME_ATTEMPT_NOT_FOUND_MESSAGE = "Game attempt not found";
    private static final String INVALID_GAME_MODE_MESSAGE_PREFIX = "Invalid game mode: ";
    private static final String GAME_ATTEMPT_ACCESS_DENIED_MESSAGE = "You do not have permission to access this game attempt.";

    private final GameAttemptRepository gameAttemptRepository;
    private final GameAttemptMapper gameAttemptMapper;
    private final GameQuestionResultRepository gameQuestionResultRepository;
    private final GameQuestionResultMapper gameQuestionResultMapper;

    @Transactional(readOnly = true)
    public Page<GameAttemptSummaryDTO> getMyGameAttempts(Long setId, String mode, LocalDate from, LocalDate to,
                                                         Pageable pageable) {
        return getMyGameAttemptsWithFilters(setId, mode, from, to, pageable);
    }

    @Transactional(readOnly = true)
    public Page<GameAttemptSummaryDTO> getMyGameAttemptsBySetId(Long setId, String mode, LocalDate from, LocalDate to,
                                                                Pageable pageable) {
        return getMyGameAttemptsWithFilters(setId, mode, from, to, pageable);
    }

    @Transactional(readOnly = true)
    public GameAttemptDTO getGameAttemptById(Long attemptId) {
        User authenticatedUser = SecurityUtils.getAuthenticatedUser();

        GameAttempt gameAttempt = gameAttemptRepository.findWithQuestionResultsByAttemptId(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, GAME_ATTEMPT_NOT_FOUND_MESSAGE));

        verifyOwnerOrAdmin(gameAttempt, authenticatedUser);

        return gameAttemptMapper.toDto(gameAttempt);
    }

    private Page<GameAttemptSummaryDTO> getMyGameAttemptsWithFilters(Long setId, String mode, LocalDate from,
                                                                     LocalDate to, Pageable pageable) {
        User authenticatedUser = SecurityUtils.getAuthenticatedUser();

        GameMode parsedMode = parseGameMode(mode);
        Instant fromDateTime = toStartOfDay(from);
        Instant toDateTimeExclusive = toExclusiveEndDate(to);

        return gameAttemptRepository.findByUserIdWithFilters(
                authenticatedUser.getUserId(),
                setId,
                parsedMode,
                fromDateTime,
                toDateTimeExclusive,
                pageable
        ).map(gameAttemptMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public List<GameQuestionResultDTO> getQuestionResultsByAttemptId(Long attemptId) {
        User authenticatedUser = SecurityUtils.getAuthenticatedUser();

        GameAttempt gameAttempt = gameAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, GAME_ATTEMPT_NOT_FOUND_MESSAGE));

        verifyOwnerOrAdmin(gameAttempt, authenticatedUser);

        return gameQuestionResultRepository.findByAttempt_AttemptIdOrderByQuestionOrderAsc(attemptId)
                .stream()
                .map(gameQuestionResultMapper::toDto)
                .toList();
    }

    private GameMode parseGameMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return null;
        }

        try {
            return GameMode.fromValue(mode);
        } catch (IllegalArgumentException _) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_GAME_MODE_MESSAGE_PREFIX + mode);
        }
    }

    private Instant toStartOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay().toInstant(DATE_FILTER_ZONE_OFFSET);
    }

    private Instant toExclusiveEndDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.plusDays(1).atStartOfDay().toInstant(DATE_FILTER_ZONE_OFFSET);
    }

    private void verifyOwnerOrAdmin(GameAttempt gameAttempt, User authenticatedUser) {
        boolean isOwner = gameAttempt.getUser().getUserId().equals(authenticatedUser.getUserId());

        if (!isOwner && !SecurityUtils.isAdmin(authenticatedUser)) {
            throw new AccessDeniedException(GAME_ATTEMPT_ACCESS_DENIED_MESSAGE);
        }
    }
}