package com.brainbooster.flashcardset;

import com.brainbooster.user.User;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
class FlashcardSetRepositoryTest {

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Long savedUserId;
    private Long savedSetId;

    @BeforeEach
    void setUp() {
        // save test user in DB
        User user = TestEntities.userBuilder()
                .userId(null) // null forces the generation of an ID in the database
                .nickname("unique_nickname")
                .email("test_user@example.com")
                .build();
        User savedUser = entityManager.persist(user);
        savedUserId = savedUser.getUserId();

        // save 2 sets for user
        FlashcardSet set1 = TestEntities.flashcardSetBuilder()
                .setId(null)
                .user(savedUser)
                .setName("Java Basics")
                .build();

        FlashcardSet set2 = TestEntities.flashcardSetBuilder()
                .setId(null)
                .user(savedUser)
                .setName("Spring Boot Advanced")
                .build();

        FlashcardSet persistedSet1 = entityManager.persist(set1);
        entityManager.persist(set2);
        entityManager.flush();

        savedSetId = persistedSet1.getSetId();
    }

    @Test
    @DisplayName("findByUserId - Should return list of flashcard sets for given user ID")
    void findByUserId_ShouldReturnSets() {
        // when
        List<FlashcardSet> result = flashcardSetRepository.findByUserId(savedUserId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FlashcardSet::getSetName)
                .containsExactlyInAnyOrder("Java Basics", "Spring Boot Advanced");
        assertThat(result.getFirst().getUser().getUserId()).isEqualTo(savedUserId);
    }

    @Test
    @DisplayName("findAllByUserNickname - Should return list of flashcard sets for given nickname")
    void findAllByUserNickname_ShouldReturnSets() {
        // when
        List<FlashcardSet> result = flashcardSetRepository.findAllByUserNickname("unique_nickname");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FlashcardSet::getSetName)
                .containsExactlyInAnyOrder("Java Basics", "Spring Boot Advanced");
        assertThat(result.getFirst().getUser().getNickname()).isEqualTo("unique_nickname");
    }

    @Test
    @DisplayName("findByIdWithUser - Should return specific set with fetched user")
    void findByIdWithUser_ShouldReturnSet() {
        // when
        Optional<FlashcardSet> result = flashcardSetRepository.findByIdWithUser(savedSetId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getSetName()).isEqualTo("Java Basics");
        assertThat(result.get().getUser()).isNotNull();
        assertThat(result.get().getUser().getEmail()).isEqualTo("test_user@example.com");
    }

    @Test
    @DisplayName("findAllWithUsers - Should return all sets with their users")
    void findAllWithUsers_ShouldReturnAllSets() {
        // given
        User anotherUser = TestEntities.userBuilder()
                .userId(null)
                .nickname("another")
                .email("another@example.com")
                .build();
        entityManager.persist(anotherUser);

        FlashcardSet set3 = TestEntities.flashcardSetBuilder()
                .setId(null)
                .user(anotherUser)
                .setName("React Setup")
                .build();
        entityManager.persist(set3);
        entityManager.flush();

        // when
        List<FlashcardSet> result = flashcardSetRepository.findAllWithUsers();

        // then
        assertThat(result).hasSizeGreaterThanOrEqualTo(3); // Expecting at least 3 sets: 2 from setUp()
        // + 1 new set here (+ any existing in test DB)
        assertThat(result).extracting(FlashcardSet::getSetName)
                .contains("Java Basics", "Spring Boot Advanced", "React Setup");
        // Check if Users were properly fetched
        assertThat(result).extracting(set -> set.getUser().getNickname())
                .contains("unique_nickname", "another");
    }

    @Test
    @DisplayName("findAllByUserNickname - Should return empty list when nickname does not exist")
    void findAllByUserNickname_ShouldReturnEmptyList_WhenNicknameNotFound() {
        // when
        List<FlashcardSet> result = flashcardSetRepository.findAllByUserNickname("non_existent_nickname");

        // then
        assertThat(result).isEmpty();
    }
}