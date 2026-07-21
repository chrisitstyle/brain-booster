package com.brainbooster.flashcard.starred;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.integration.AbstractRepositoryTest;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserStarredFlashcardRepositoryTest extends AbstractRepositoryTest {

    private static final Instant CREATED_AT =
            Instant.parse("2024-01-01T00:00:00Z");

    @Autowired
    private UserStarredFlashcardRepository starredFlashcardRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User savedUser;
    private FlashcardSet savedFlashcardSet;
    private Flashcard savedFlashcard1;
    private Flashcard savedFlashcard2;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .nickname("testUser")
                .email("test@test.com")
                .password("pass")
                .role(Role.USER)
                .createdAt(CREATED_AT)
                .build();

        savedUser = entityManager.persist(user);

        FlashcardSet flashcardSet = FlashcardSet.builder()
                .user(savedUser)
                .setName("Java Basics")
                .description("Basic concepts")
                .createdAt(CREATED_AT)
                .build();

        savedFlashcardSet = entityManager.persist(flashcardSet);

        savedFlashcard1 = entityManager.persist(
                new Flashcard(null, savedFlashcardSet, "Term 1", "Definition 1")
        );

        savedFlashcard2 = entityManager.persist(
                new Flashcard(null, savedFlashcardSet, "Term 2", "Definition 2")
        );

        entityManager.flush();
    }

    @Test
    @DisplayName("existsByUser_UserIdAndFlashcard_FlashcardId - Should return true when flashcard is starred")
    void existsByUserIdAndFlashcardId_ShouldReturnTrue_WhenFlashcardIsStarred() {
        // given
        persistStarredFlashcard(savedUser, savedFlashcard1);

        // when
        boolean exists = starredFlashcardRepository
                .existsByUser_UserIdAndFlashcard_FlashcardId(
                        savedUser.getUserId(),
                        savedFlashcard1.getFlashcardId()
                );

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUser_UserIdAndFlashcard_FlashcardId - Should return false when flashcard is not starred")
    void existsByUserIdAndFlashcardId_ShouldReturnFalse_WhenFlashcardIsNotStarred() {
        // when
        boolean exists = starredFlashcardRepository
                .existsByUser_UserIdAndFlashcard_FlashcardId(
                        savedUser.getUserId(),
                        savedFlashcard1.getFlashcardId()
                );

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("deleteByUser_UserIdAndFlashcard_FlashcardId - Should remove starred flashcard relation")
    void deleteByUserIdAndFlashcardId_ShouldRemoveStarredRelation() {
        // given
        persistStarredFlashcard(savedUser, savedFlashcard1);

        // when
        starredFlashcardRepository.deleteByUser_UserIdAndFlashcard_FlashcardId(
                savedUser.getUserId(),
                savedFlashcard1.getFlashcardId()
        );

        entityManager.flush();
        entityManager.clear();

        // then
        boolean exists = starredFlashcardRepository
                .existsByUser_UserIdAndFlashcard_FlashcardId(
                        savedUser.getUserId(),
                        savedFlashcard1.getFlashcardId()
                );

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("findStarredFlashcardIdsByUserId - Should return starred flashcard IDs for user")
    void findStarredFlashcardIdsByUserId_ShouldReturnStarredFlashcardIds() {
        // given
        persistStarredFlashcard(savedUser, savedFlashcard1);
        persistStarredFlashcard(savedUser, savedFlashcard2);

        // when
        Set<Long> result = starredFlashcardRepository
                .findStarredFlashcardIdsByUserId(savedUser.getUserId());

        // then
        assertThat(result)
                .containsExactlyInAnyOrder(
                        savedFlashcard1.getFlashcardId(),
                        savedFlashcard2.getFlashcardId()
                );
    }

    @Test
    @DisplayName("findStarredFlashcardIdsByUserIdAndSetId - Should return starred flashcard IDs only from selected set")
    void findStarredFlashcardIdsByUserIdAndSetId_ShouldReturnStarredFlashcardIdsFromSelectedSet() {
        // given
        persistStarredFlashcard(savedUser, savedFlashcard1);

        // when
        Set<Long> result = starredFlashcardRepository
                .findStarredFlashcardIdsByUserIdAndSetId(
                        savedUser.getUserId(),
                        savedFlashcardSet.getSetId()
                );

        // then
        assertThat(result).containsExactly(savedFlashcard1.getFlashcardId());
    }

    private void persistStarredFlashcard(User user, Flashcard flashcard) {
        UserStarredFlashcard starredFlashcard = UserStarredFlashcard.builder()
                .id(new UserStarredFlashcardId(
                        user.getUserId(),
                        flashcard.getFlashcardId()
                ))
                .user(user)
                .flashcard(flashcard)
                .createdAt(CREATED_AT)
                .build();

        entityManager.persist(starredFlashcard);
        entityManager.flush();
    }
}