package com.brainbooster.flashcardset;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
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
class FlashcardRepositoryTest {

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Long savedSetId;

    @BeforeEach
    void setUp() {
        User user = TestEntities.userBuilder()
                .userId(null)
                .nickname("testuser")
                .email("testuser@test.com")
                .build();
        entityManager.persist(user);

        FlashcardSet flashcardSet = TestEntities.flashcardSetBuilder()
                .setId(null)
                .user(user)
                .setName("Java Basics")
                .description("Basic concepts")
                .build();
        entityManager.persist(flashcardSet);

        savedSetId = flashcardSet.getSetId();
    }

    @Test
    @DisplayName("findAllBySetId - Should return list of flashcards when exists")
    void findAllBySetId_ShouldReturnFlashcards() {
        // given
        Flashcard f1 = TestEntities.flashcardBuilder()
                .flashcardId(null)
                .setId(savedSetId)
                .term("Term 1")
                .definition("Def 1")
                .build();

        Flashcard f2 = TestEntities.flashcardBuilder()
                .flashcardId(null)
                .setId(savedSetId)
                .term("Term 2")
                .definition("Def 2")
                .build();

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
        Long nonExistentSetId = 997L;

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
        Flashcard flashcard = TestEntities.flashcardBuilder()
                .flashcardId(null) // null because new object
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
        Flashcard flashcard = TestEntities.flashcardBuilder()
                .flashcardId(null)
                .setId(savedSetId)
                .term("Polymorphism")
                .definition("Many forms")
                .build();

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
        Flashcard f1 = TestEntities.flashcardBuilder()
                .flashcardId(null).setId(savedSetId).term("A").definition("Def A").build();
        Flashcard f2 = TestEntities.flashcardBuilder()
                .flashcardId(null).setId(savedSetId).term("C").definition("Def C").build();

        entityManager.persist(f1);
        entityManager.persist(f2);
        entityManager.flush();

        // when
        List<Flashcard> allFlashcardsFromDB = flashcardRepository.findAll();

        // then
        assertThat(allFlashcardsFromDB).extracting(Flashcard::getTerm)
                .contains("A", "C");
    }

    @Test
    @DisplayName("update - Should update existing flashcard")
    void update_ShouldModifyData() {
        // given
        Flashcard flashcard = TestEntities.flashcardBuilder()
                .flashcardId(null)
                .setId(savedSetId)
                .term("Old Term")
                .definition("Old Def")
                .build();

        Flashcard persistedFlashcard = entityManager.persist(flashcard);
        entityManager.flush();

        // when
        persistedFlashcard.setTerm("New Term");
        Flashcard updatedFlashcard = flashcardRepository.save(persistedFlashcard);

        // then
        assertThat(updatedFlashcard).isNotNull();
        assertThat(updatedFlashcard.getTerm()).isEqualTo("New Term");

        // checking db
        Optional<Flashcard> flashcardFromDB = flashcardRepository.findById(persistedFlashcard.getFlashcardId());
        assertThat(flashcardFromDB).isPresent();
        assertThat(flashcardFromDB.get().getTerm()).isEqualTo("New Term");
    }

    @Test
    @DisplayName("deleteById - Should remove flashcard")
    void deleteById_ShouldRemoveFlashcard() {
        // given
        Flashcard flashcard = TestEntities.flashcardBuilder()
                .flashcardId(null)
                .setId(savedSetId)
                .term("To Delete Flashcard")
                .build();

        Flashcard persisted = entityManager.persist(flashcard);
        Long id = persisted.getFlashcardId();
        entityManager.flush();

        // when
        flashcardRepository.deleteById(id);

        // then
        Optional<Flashcard> result = flashcardRepository.findById(id);
        assertThat(result).isEmpty();
    }
}
