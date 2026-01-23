package com.brainbooster.flashcard;

import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
class FlashcardRepositoryTest {

    @Autowired
    private FlashcardRepository flashcardRepository;
    @Autowired
    private TestEntityManager entityManager;
    private Long savedSetId;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .nickname("testUser")
                .email("test@test.com")
                .password("pass")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(user);

        FlashcardSet flashcardSet = FlashcardSet.builder()
                .user(user)
                .setName("Java Basics")
                .description("Basic concepts")
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(flashcardSet);

        savedSetId = flashcardSet.getSetId();
    }

    @Test
    @DisplayName("findAllBySetId - Should return list of flashcards when exists")
    void findAllBySetId_ShouldReturnFlashcards() {
        // given
        Flashcard f1 = new Flashcard(null, savedSetId, "Term 1", "Def 1");
        Flashcard f2 = new Flashcard(null, savedSetId, "Term 2", "Def 2");

        entityManager.persist(f1);
        entityManager.persist(f2);
        entityManager.flush();

        // when
        Optional<List<Flashcard>> result = flashcardRepository.findAllBySetId(savedSetId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(2);
        assertThat(result.get()).extracting(Flashcard::getTerm)
                .containsExactlyInAnyOrder("Term 1", "Term 2");
    }

    @Test
    @DisplayName("findAllBySetId - Should return empty list when set has no flashcards")
    void findAllBySetId_ShouldReturnEmptyList_WhenNoFlashcards() {
        // given

        // when
        Optional<List<Flashcard>> result = flashcardRepository.findAllBySetId(savedSetId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEmpty();
    }

    @Test
    @DisplayName("findAllBySetId - Should return empty list when set ID does not exist")
    void findAllBySetId_ShouldReturnEmpty_WhenSetIdDoesNotExist() {
        // given
        Long nonExistentSetId = 9999L;

        // when
        Optional<List<Flashcard>> result = flashcardRepository.findAllBySetId(nonExistentSetId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEmpty();
    }

    @Test
    @DisplayName("save - Should generate ID and persist flashcard")
    void save_ShouldPersistFlashcard() {
        // given
        Flashcard flashcard = Flashcard.builder()
                .setId(savedSetId)
                .term("Inheritance")
                .definition("Is-a relationship")
                .build();

        // when
        Flashcard saved = flashcardRepository.save(flashcard);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getFlashcardId()).isNotNull();
        assertThat(saved.getTerm()).isEqualTo("Inheritance");
    }

    @Test
    @DisplayName("findById - Should return flashcard when exists")
    void findById_ShouldReturnFlashcard() {
        // given
        Flashcard flashcard = new Flashcard(null, savedSetId, "Polymorphism", "Many forms");
        Flashcard persisted = entityManager.persist(flashcard);
        Long id = persisted.getFlashcardId();

        // when
        Optional<Flashcard> result = flashcardRepository.findById(id);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getDefinition()).isEqualTo("Many forms");
    }

    @Test
    @DisplayName("findAll - Should return all flashcards in DB")
    void findAll_ShouldReturnAll() {
        // given
        entityManager.persist(new Flashcard(null, savedSetId, "A", "B"));
        entityManager.persist(new Flashcard(null, savedSetId, "C", "D"));

        // when
        List<Flashcard> all = flashcardRepository.findAll();

        // then
        assertThat(all).extracting(Flashcard::getTerm)
                .contains("A", "C");
    }

    @Test
    @DisplayName("update - Should update existing flashcard")
    void update_ShouldModifyData() {
        // given
        Flashcard flashcard = new Flashcard(null, savedSetId, "Old Term", "Old Def");
        Flashcard persisted = entityManager.persist(flashcard);

        // when
        persisted.setTerm("New Term");
        Flashcard updated = flashcardRepository.save(persisted);

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getTerm()).isEqualTo("New Term");

        Optional<Flashcard> fromDb = flashcardRepository.findById(persisted.getFlashcardId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getTerm()).isEqualTo("New Term");
    }

    @Test
    @DisplayName("deleteById - Should remove flashcard")
    void deleteById_ShouldRemoveFlashcard() {
        // given
        Flashcard flashcard = new Flashcard(null, savedSetId, "To Delete", "Def");
        Flashcard persisted = entityManager.persist(flashcard);
        Long id = persisted.getFlashcardId();

        // when
        flashcardRepository.deleteById(id);

        // then
        Optional<Flashcard> result = flashcardRepository.findById(id);
        assertThat(result).isEmpty();
    }
}
