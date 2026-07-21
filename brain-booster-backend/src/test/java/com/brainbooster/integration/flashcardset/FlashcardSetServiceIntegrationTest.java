package com.brainbooster.integration.flashcardset;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcard.dto.FlashcardDTO;
import com.brainbooster.flashcard.starred.UserStarredFlashcardRepository;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.flashcardset.FlashcardSetService;
import com.brainbooster.flashcardset.dto.FlashcardSetCreationDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetDTO;
import com.brainbooster.flashcardset.dto.FlashcardSetUpdateDTO;
import com.brainbooster.integration.AbstractIntegrationTest;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Sql(scripts = "/insert-it-test-users.sql")
class FlashcardSetServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private FlashcardSetService flashcardSetService;
    @Autowired
    private FlashcardSetRepository flashcardSetRepository;
    @Autowired
    private FlashcardRepository flashcardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserStarredFlashcardRepository starredFlashcardRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(User user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // -- CREATE TESTS --

    @Test
    @DisplayName("addFlashcardSet - Should save and return FlashcardSetDTO")
    void addFlashcardSet_ShouldSaveSet() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        FlashcardSetCreationDTO creationDTO = new FlashcardSetCreationDTO(
                "My Set",
                "My Desc");

        // when
        FlashcardSetDTO result = flashcardSetService.addFlashcardSet(creationDTO, owner.getUserId());

        // then
        assertThat(result.setName()).isEqualTo("My Set");
        assertThat(result.description()).isEqualTo("My Desc");
        assertThat(flashcardSetRepository.findById(result.setId())).isPresent();
    }

    @Test
    @DisplayName("addFlashcardSet - Should throw NoSuchElementException when user does not exist")
    void addFlashcardSet_ShouldThrowNoSuchElementException_WhenUserNotFound() {
        // given
        FlashcardSetCreationDTO creationDTO = new FlashcardSetCreationDTO(
                "My Set",
                "My Desc");

        // when, then
        assertThatThrownBy(() -> flashcardSetService.addFlashcardSet(creationDTO, 999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("User with id: 999 not found");
    }

    // -- READ TESTS --

    @Test
    @DisplayName("getAllFlashcardSets - Should return all flashcard sets")
    void getAllFlashcardSets_ShouldReturnList() {
        // given
        User user = userRepository.findById(2L).orElseThrow();
        flashcardSetRepository.save(TestEntities
                .flashcardSetBuilder()
                .setId(null)
                .user(user)
                .setName("Set 1").build());

        flashcardSetRepository.save(TestEntities
                .flashcardSetBuilder()
                .setId(null)
                .user(user)
                .setName("Set 2").build());

        // when
        List<FlashcardSetDTO> results = flashcardSetService.getAllFlashcardSets();

        // then
        assertThat(results).isNotEmpty();
        assertThat(results).extracting(FlashcardSetDTO::setName).contains("Set 1", "Set 2");
    }

    @Test
    @DisplayName("getFlashcardSetById - Should return flashcard set when it exists")
    void getFlashcardSetById_ShouldReturnSet() {
        // given
        User user = userRepository.findById(2L).orElseThrow();
        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(user)
                        .setName("Specific Set").build());

        // when
        FlashcardSetDTO result = flashcardSetService.getFlashcardSetById(savedSet.getSetId());

        // then
        assertThat(result.setName()).isEqualTo("Specific Set");
    }

    @Test
    @DisplayName("getFlashcardSetById - Should throw NoSuchElementException when set not found")
    void getFlashcardSetById_ShouldThrowNoSuchElementException() {
        // when, then
        assertThatThrownBy(() -> flashcardSetService.getFlashcardSetById(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("FlashcardSet with id: 999 not found");
    }

    @Test
    @DisplayName("getAllFlashcardsInSet - Should return unstarred flashcards for unauthenticated user")
    void getAllFlashcardsInSet_ShouldReturnFlashcards_WhenUnauthenticated() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(owner).build());

        flashcardRepository.save(
                TestEntities
                        .flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(savedSet)
                        .term("Card 1").build());

        flashcardRepository.save(TestEntities
                .flashcardBuilder()
                .flashcardId(null)
                .flashcardSet(savedSet)
                .term("Card 2").build());

        SecurityContextHolder.clearContext(); // explicitly no user

        // when
        List<FlashcardDTO> results = flashcardSetService.getAllFlashcardsInSet(savedSet.getSetId());

        // then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(FlashcardDTO::starred).containsOnly(false);
    }

    @Test
    @DisplayName("getAllFlashcardsInSet - Should return mapped starred flags for authenticated user")
    void getAllFlashcardsInSet_ShouldReturnFlashcards_WithStarredFlags_WhenAuthenticated() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        mockAuthenticatedUser(owner);

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(owner).build());

        Flashcard card1 = flashcardRepository.save(
                TestEntities
                        .flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(savedSet)
                        .term("Card 1").build());

        Flashcard card2 = flashcardRepository.save(
                TestEntities
                        .flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(savedSet)
                        .term("Card 2").build());

        // Star card1 only
        starredFlashcardRepository.save(TestEntities.createUserStarredFlashcard(owner, card1));

        // when
        List<FlashcardDTO> results = flashcardSetService.getAllFlashcardsInSet(savedSet.getSetId());

        // then
        assertThat(results).hasSize(2);
        FlashcardDTO dto1 = results.stream()
                .filter(r -> r.flashcardId()
                        .equals(card1.getFlashcardId())).findFirst().orElseThrow();

        FlashcardDTO dto2 = results.stream()
                .filter(r -> r.flashcardId()
                        .equals(card2.getFlashcardId())).findFirst().orElseThrow();

        assertThat(dto1.starred()).isTrue();
        assertThat(dto2.starred()).isFalse();
    }

    @Test
    @DisplayName("getAllFlashcardsInSet - Should throw NoSuchElementException when set does not exist")
    void getAllFlashcardsInSet_ShouldThrowNoSuchElementException() {
        // when, then
        assertThatThrownBy(() -> flashcardSetService.getAllFlashcardsInSet(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("FlashcardSet with id: 999 not found");
    }

    // -- UPDATE TESTS --

    @Test
    @DisplayName("updateFlashcardSet - Should modify set when called by owner")
    void updateFlashcardSet_ShouldModifySet() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        mockAuthenticatedUser(owner);

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(owner)
                        .setName("Old Name").build());

        FlashcardSetUpdateDTO updateDTO = new FlashcardSetUpdateDTO(
                "Updated Name",
                "Updated Desc");

        // when
        FlashcardSetDTO result = flashcardSetService.updateFlashcardSet(updateDTO, savedSet.getSetId());

        // then
        assertThat(result.setName()).isEqualTo("Updated Name");
        assertThat(result.description()).isEqualTo("Updated Desc");

        FlashcardSet updatedSetFromDB = flashcardSetRepository.findById(savedSet.getSetId()).orElseThrow();
        assertThat(updatedSetFromDB.getSetName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("updateFlashcardSet - Should throw AccessDeniedException when called by non-owner")
    void updateFlashcardSet_ShouldThrowAccessDeniedException_WhenNotOwner() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        User anotherUser = userRepository.findById(3L).orElseThrow();
        mockAuthenticatedUser(anotherUser);

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(owner).build());
        FlashcardSetUpdateDTO updateDTO = new FlashcardSetUpdateDTO(
                "Hacked Name",
                "Hacked Desc");

        Long setId = savedSet.getSetId();

        // when, then
        assertThatThrownBy(() -> flashcardSetService.updateFlashcardSet(updateDTO, setId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You are not allowed to edit this flashcard set!");
    }

    @Test
    @DisplayName("updateFlashcardSet - Should throw NoSuchElementException when set not found")
    void updateFlashcardSet_ShouldThrowNoSuchElementException() {
        // given
        FlashcardSetUpdateDTO updateDTO = new FlashcardSetUpdateDTO(
                "New Name",
                "New Desc");

        // when, then
        assertThatThrownBy(() -> flashcardSetService.updateFlashcardSet(updateDTO, 999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("FlashcardSet with id: 999 not found");
    }

    // -- DELETE TESTS --

    @Test
    @DisplayName("deleteFlashcardSetById - Should delete set when called by owner")
    void deleteFlashcardSetById_ShouldRemoveFromDB() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        mockAuthenticatedUser(owner);

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(owner).build());

        Long setId = savedSet.getSetId();

        // when
        flashcardSetService.deleteFlashcardSetById(setId);

        // then
        assertThat(flashcardSetRepository.findById(setId)).isEmpty();
    }

    @Test
    @DisplayName("deleteFlashcardSetById - Should throw AccessDeniedException when called by non-owner")
    void deleteFlashcardSetById_ShouldThrowAccessDeniedException_WhenNotOwner() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        User anotherUser = userRepository.findById(3L).orElseThrow();
        mockAuthenticatedUser(anotherUser);

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(owner).build());

        Long setId = savedSet.getSetId();

        // when, then
        assertThatThrownBy(() -> flashcardSetService.deleteFlashcardSetById(setId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You are not allowed to delete this flashcard set!");
    }
}
