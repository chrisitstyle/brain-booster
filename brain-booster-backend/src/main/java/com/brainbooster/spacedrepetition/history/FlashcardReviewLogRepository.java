package com.brainbooster.spacedrepetition.history;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FlashcardReviewLogRepository extends JpaRepository<FlashcardReviewLog, Long> {

    Optional<FlashcardReviewLog> findByRequestId(UUID requestId);

    List<FlashcardReviewLog> findByReviewState_User_UserIdOrderByReviewedAtDesc(Long userId);
}
