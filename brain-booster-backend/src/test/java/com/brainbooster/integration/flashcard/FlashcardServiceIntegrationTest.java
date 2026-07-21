package com.brainbooster.integration.flashcard;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcard.FlashcardService;
import com.brainbooster.flashcard.dto.FlashcardCreationDTO;
import com.brainbooster.flashcard.dto.FlashcardDTO;
import com.brainbooster.flashcard.dto.FlashcardUpdateDTO;
import com.brainbooster.flashcard.starred.UserStarredFlashcardRepository;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
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
class FlashcardServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private FlashcardService flashcardService;
    @Autowired
    private FlashcardRepository flashcardRepository;
    @Autowired
    private FlashcardSetRepository flashcardSetRepository;
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
    @DisplayName("addFlashcard - Should save and return FlashcardDTO when called by owner")
    void addFlashcard_ShouldSaveFlashcard() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        mockAuthenticatedUser(owner);

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(owner).build());

        FlashcardCreationDTO creationDTO = new FlashcardCreationDTO(
                savedSet.getSetId(),
                "term",
                "definition");

        // when
        FlashcardDTO result = flashcardService.addFlashcard(creationDTO);

        // then
        assertThat(result.term()).isEqualTo("term");
        assertThat(flashcardRepository.findById(result.flashcardId())).isPresent();
    }

    @Test
    @DisplayName("addFlashcard - Should throw AccessDeniedException when called by non-owner")
    void addFlashcard_ShouldThrowAccessDeniedException_WhenNotOwner() {
        // given
        User regularUser = userRepository.findById(2L).orElseThrow();
        User anotherUser = userRepository.findById(3L).orElseThrow();

        mockAuthenticatedUser(anotherUser);

        FlashcardSet savedRegularUserSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(regularUser)
                        .build());

        FlashcardCreationDTO creationDTO = new FlashcardCreationDTO(
                savedRegularUserSet.getSetId(),
                "term",
                "definition");

        // when, then
        assertThatThrownBy(() -> flashcardService.addFlashcard(creationDTO))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only add flashcards to your own sets!");
    }

    @Test
    @DisplayName("addFlashcard - Should throw NoSuchElementException when set does not exist")
    void addFlashcard_ShouldThrowNoSuchElementException_WhenSetNotFound() {
        // given
        FlashcardCreationDTO creationDTO = new FlashcardCreationDTO(
                999L,
                "term",
                "definition");

        // when, then
        assertThatThrownBy(() -> flashcardService.addFlashcard(creationDTO))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("FlashcardSet with id 999 not found");
    }

    // -- READ TESTS --

    @Test
    @DisplayName("getAllFlashcards - Should return all flashcards")
    void getAllFlashcards_ShouldReturnList() {
        // given
        User user = userRepository.findById(2L).orElseThrow();
        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(user).build());

        flashcardRepository.save(
                TestEntities
                        .flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(savedSet).build());

        flashcardRepository.save(
                TestEntities
                        .flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(savedSet).build());

        // when
        List<FlashcardDTO> results = flashcardService.getAllFlashcards();

        // then
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("getFlashcardById - Should return flashcard when it exists")
    void getFlashcardById_ShouldReturnFlashcard() {
        // given
        User user = userRepository.findById(2L).orElseThrow();
        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(user).build());

        Flashcard savedCard = flashcardRepository.save(
                TestEntities
                        .flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(savedSet)
                        .term("Specific Term").build());

        // when
        FlashcardDTO result = flashcardService.getFlashcardById(savedCard.getFlashcardId());

        // then
        assertThat(result.term()).isEqualTo("Specific Term");
    }

    @Test
    @DisplayName("getFlashcardById - Should throw NoSuchElementException when flashcard not found")
    void getFlashcardById_ShouldThrowNoSuchElementException() {
        // when, then
        assertThatThrownBy(() -> flashcardService.getFlashcardById(999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    // -- UPDATE TESTS --

    @Test
    @DisplayName("updateFlashcard - Should update flashcard when called by owner")
    void updateFlashcard_ShouldModifyCard() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        mockAuthenticatedUser(owner);

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(owner).build());

        Flashcard savedCard = flashcardRepository.save(
                TestEntities
                        .flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(savedSet)
                        .term("Old Term").build());

        FlashcardUpdateDTO updateDTO = new FlashcardUpdateDTO("New Term", "New Def");

        // when
        FlashcardDTO result = flashcardService.updateFlashcard(updateDTO, savedCard.getFlashcardId());

        // then
        assertThat(result.term()).isEqualTo("New Term");

        Flashcard updatedCardFromDB = flashcardRepository.findById(savedCard.getFlashcardId()).orElseThrow();
        assertThat(updatedCardFromDB.getTerm()).isEqualTo("New Term");
    }

    @Test
    @DisplayName("updateFlashcard - Should throw AccessDeniedException when called by non-owner")
    void updateFlashcard_ShouldThrowAccessDeniedException_WhenNotOwner() {
        // given
        User regularUser = userRepository.findById(2L).orElseThrow(); // owner
        User anotherUser = userRepository.findById(3L).orElseThrow(); // user trying to access

        mockAuthenticatedUser(anotherUser);

        FlashcardSet regularUserSet = flashcardSetRepository.save(TestEntities
                .flashcardSetBuilder()
                .setId(null)
                .user(regularUser).build());

        Flashcard savedRegularUserCard = flashcardRepository.save(
                TestEntities
                        .flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(regularUserSet).build());

        FlashcardUpdateDTO updateDTO = new FlashcardUpdateDTO("New Term", "New Def");

        Long flashcardId = savedRegularUserCard.getFlashcardId();
        // when, then
        assertThatThrownBy(() -> flashcardService.updateFlashcard(updateDTO, flashcardId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You are not allowed to edit this flashcard!");
    }

    // -- DELETE TESTS --
    @Test
    @DisplayName("deleteFlashcardById - Should delete flashcard when called by owner")
    void deleteFlashcardById_ShouldRemoveFromDB() {
        // given
        User owner = userRepository.findById(2L).orElseThrow();
        mockAuthenticatedUser(owner);

        FlashcardSet savedSet = flashcardSetRepository.save(TestEntities.flashcardSetBuilder().setId(null).user(owner).build());
        Flashcard savedCard = flashcardRepository.save(TestEntities.flashcardBuilder().flashcardId(null).flashcardSet(savedSet).build());

        // when
        flashcardService.deleteFlashcardById(savedCard.getFlashcardId());

        // then
        assertThat(flashcardRepository.findById(savedCard.getFlashcardId())).isEmpty();
    }

    @Test
    @DisplayName("deleteFlashcardById - Should throw AccessDeniedException when called by non-owner")
    void deleteFlashcardById_ShouldThrowAccessDeniedException() {
        // given
        User owner = userRepository.findById(1L).orElseThrow();
        User anotherUser = userRepository.findById(2L).orElseThrow();

        mockAuthenticatedUser(anotherUser);

        FlashcardSet savedOwnerSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(owner).build());

        Flashcard savedOwnerCard = flashcardRepository.save(
                TestEntities
                        .flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(savedOwnerSet).build());

        Long flashcardId = savedOwnerCard.getFlashcardId();

        // when, then
        assertThatThrownBy(() -> flashcardService.deleteFlashcardById(flashcardId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You are not allowed to delete this flashcard!");
    }

    // -- STARRED TESTS --

    @Test
    @DisplayName("starFlashcard - Should add star relation for authenticated user")
    void starFlashcard_ShouldSaveStarRelation() {
        // given
        User user = userRepository.findById(2L).orElseThrow();
        mockAuthenticatedUser(user);

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(user).build());

        Flashcard savedCard = flashcardRepository.save(
                TestEntities
                        .flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(savedSet).build());

        // when
        FlashcardDTO result = flashcardService.starFlashcard(savedCard.getFlashcardId());

        // then
        assertThat(result.starred()).isTrue();
        assertThat(starredFlashcardRepository
                .existsByUser_UserIdAndFlashcard_FlashcardId(
                        user.getUserId(),
                        savedCard.getFlashcardId())).isTrue();
    }

    @Test
    @DisplayName("unstarFlashcard - Should remove star relation for authenticated user")
    void unstarFlashcard_ShouldRemoveStarRelation() {
        // given
        User user = userRepository.findById(2L).orElseThrow();
        mockAuthenticatedUser(user);

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities
                        .flashcardSetBuilder()
                        .setId(null)
                        .user(user).build());

        Flashcard savedCard = flashcardRepository.save(
                TestEntities
                        .flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(savedSet).build());

        // simulate that user already starred it
        starredFlashcardRepository.save(TestEntities.createUserStarredFlashcard(user, savedCard));

        // when
        FlashcardDTO result = flashcardService.unstarFlashcard(savedCard.getFlashcardId());

        // then
        assertThat(result.starred()).isFalse();
        assertThat(starredFlashcardRepository
                .existsByUser_UserIdAndFlashcard_FlashcardId(
                        user.getUserId(),
                        savedCard.getFlashcardId())).isFalse();
    }
}
