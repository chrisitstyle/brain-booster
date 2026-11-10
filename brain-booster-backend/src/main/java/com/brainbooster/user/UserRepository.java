package com.brainbooster.user;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    boolean existsByNickname(String nickName);

    /**
     * Locks the user row for the duration of a review transaction.
     *
     * <p>Serializing review submissions per user prevents two requests
     * from simultaneously creating the first review state for a card.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT user FROM User user WHERE user.userId = :userId")
    Optional<User> findByIdForReviewUpdate(@Param("userId") Long userId);
}
