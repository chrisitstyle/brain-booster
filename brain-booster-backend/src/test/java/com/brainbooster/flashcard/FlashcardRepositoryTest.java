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

    private FlashcardSet savedFlashcardSet;

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

        savedFlashcardSet = entityManager.persist(flashcardSet);
    }

    @Test
    @DisplayName("findAllByFlashcardSet_SetId - Should return list of flashcards when exists")
    void findAllByFlashcardSetSetId_ShouldReturnFlashcards() {
        // given
        Flashcard f1 = new Flashcard(null, savedFlashcardSet, "Term 1", "Def 1");
        Flashcard f2 = new Flashcard(null, savedFlashcardSet, "Term 2", "Def 2");

        entityManager.persist(f1);
        entityManager.persist(f2);
        entityManager.flush();

        // when
        List<Flashcard> result = flashcardRepository.findAllByFlashcardSet_SetId(savedFlashcardSet.getSetId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Flashcard::getTerm)
                .containsExactlyInAnyOrder("Term 1", "Term 2");
    }

    @Test
    @DisplayName("findAllByFlashcardSet_SetId - Should return empty list when set has no flashcards")
    void findAllByFlashcardSetSetId_ShouldReturnEmptyList_WhenNoFlashcards() {
        // when
        List<Flashcard> result = flashcardRepository.findAllByFlashcardSet_SetId(savedFlashcardSet.getSetId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAllByFlashcardSet_SetId - Should return empty list when set ID does not exist")
    void findAllBySetId_ShouldReturnEmpty_WhenFlashcardSetSetIdDoesNotExist() {
        // given
        Long nonExistentSetId = 9999L;

        // when
        List<Flashcard> result = flashcardRepository.findAllByFlashcardSet_SetId(nonExistentSetId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("save - Should generate ID and persist flashcard")
    void save_ShouldPersistFlashcard() {
        // given
        Flashcard flashcard = Flashcard.builder()
                .flashcardSet(savedFlashcardSet)
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
        Flashcard flashcard = new Flashcard(null, savedFlashcardSet, "Polymorphism", "Many forms");
        Flashcard persisted = entityManager.persist(flashcard);
        Long id = persisted.getFlashcardId();

        // when
        Optional<Flashcard> result = flashcardRepository.findById(id);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getDefinition()).isEqualTo("Many forms");
    }

    @Test
    @DisplayName("findByIdWithSetAndUser - Should fetch entity with its Set and User")
    void findByIdWithSetAndUser_ShouldReturnEntityWithRelations() {
        // given
        Flashcard flashcard = new Flashcard(null, savedFlashcardSet, "Encapsulation", "Hiding state");
        Flashcard persisted = entityManager.persist(flashcard);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Flashcard> result = flashcardRepository.findByIdWithSetAndUser(persisted.getFlashcardId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getFlashcardSet()).isNotNull();
        assertThat(result.get().getFlashcardSet().getUser()).isNotNull();
        assertThat(result.get().getFlashcardSet().getUser().getNickname()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("findAll - Should return all flashcards in DB")
    void findAll_ShouldReturnAll() {
        // given
        entityManager.persist(new Flashcard(null, savedFlashcardSet, "A", "B"));
        entityManager.persist(new Flashcard(null, savedFlashcardSet, "C", "D"));

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
        Flashcard flashcard = new Flashcard(null, savedFlashcardSet, "Old Term", "Old Def");
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
        Flashcard flashcard = new Flashcard(null, savedFlashcardSet, "To Delete", "Def");
        Flashcard persisted = entityManager.persist(flashcard);
        Long id = persisted.getFlashcardId();

        // when
        flashcardRepository.deleteById(id);

        // then
        Optional<Flashcard> result = flashcardRepository.findById(id);
        assertThat(result).isEmpty();
    }
}
