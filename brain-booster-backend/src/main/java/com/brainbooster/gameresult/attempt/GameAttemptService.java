package com.brainbooster.gameresult.attempt;

import com.brainbooster.gameresult.dto.GameAttemptDTO;
import com.brainbooster.gameresult.mapper.GameAttemptMapper;
import com.brainbooster.user.User;
import com.brainbooster.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameAttemptService {

    private final GameAttemptRepository gameAttemptRepository;
    private final GameAttemptMapper gameAttemptMapper;

    @Transactional(readOnly = true)
    public List<GameAttemptDTO> getMyGameAttempts() {
        User authenticatedUser = SecurityUtils.getAuthenticatedUser();

        return gameAttemptRepository
                .findByUser_UserIdOrderByCompletedAtDesc(authenticatedUser.getUserId())
                .stream()
                .map(gameAttemptMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GameAttemptDTO> getMyGameAttemptsBySetId(Long setId) {
        User authenticatedUser = SecurityUtils.getAuthenticatedUser();

        return gameAttemptRepository
                .findByUser_UserIdAndSet_SetIdOrderByCompletedAtDesc(
                        authenticatedUser.getUserId(),
                        setId
                )
                .stream()
                .map(gameAttemptMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public GameAttemptDTO getGameAttemptById(Long attemptId) {
        User authenticatedUser = SecurityUtils.getAuthenticatedUser();

        GameAttempt gameAttempt = gameAttemptRepository.findWithQuestionResultsByAttemptId(attemptId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Game attempt not found"
                ));

        verifyOwnerOrAdmin(gameAttempt, authenticatedUser);

        return gameAttemptMapper.toDto(gameAttempt);
    }

    private void verifyOwnerOrAdmin(GameAttempt gameAttempt, User authenticatedUser) {
        boolean isOwner = gameAttempt.getUser()
                .getUserId()
                .equals(authenticatedUser.getUserId());

        if (!isOwner && !SecurityUtils.isAdmin(authenticatedUser)) {
            throw new AccessDeniedException(
                    "You do not have permission to access this game attempt."
            );
        }
    }
}