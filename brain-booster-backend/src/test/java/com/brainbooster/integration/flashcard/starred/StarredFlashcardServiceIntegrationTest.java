package com.brainbooster.integration.flashcard.starred;

import com.brainbooster.flashcard.Flashcard;
import com.brainbooster.flashcard.FlashcardRepository;
import com.brainbooster.flashcard.dto.FlashcardDTO;
import com.brainbooster.flashcard.starred.StarredFlashcardService;
import com.brainbooster.flashcard.starred.UserStarredFlashcardRepository;
import com.brainbooster.flashcardset.FlashcardSet;
import com.brainbooster.flashcardset.FlashcardSetRepository;
import com.brainbooster.integration.AbstractIntegrationTest;
import com.brainbooster.security.UserPrincipal;
import com.brainbooster.user.User;
import com.brainbooster.user.UserRepository;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/insert-it-test-users.sql")
class StarredFlashcardServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private StarredFlashcardService starredFlashcardService;
    @Autowired
    private UserStarredFlashcardRepository starredFlashcardRepository;
    @Autowired
    private FlashcardRepository flashcardRepository;
    @Autowired
    private FlashcardSetRepository flashcardSetRepository;
    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(User user) {
        UserPrincipal principal = UserPrincipal.from(user);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("starFlashcard - Should add star relation for authenticated user")
    void starFlashcard_ShouldSaveStarRelation() {
        // given
        User user = userRepository.findById(2L).orElseThrow();
        mockAuthenticatedUser(user);

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities.flashcardSetBuilder()
                        .setId(null)
                        .user(user)
                        .build());

        Flashcard savedCard = flashcardRepository.save(
                TestEntities.flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(savedSet)
                        .build());

        // when
        FlashcardDTO result = starredFlashcardService
                .starFlashcard(savedCard.getFlashcardId());

        // then
        assertThat(result.starred()).isTrue();

        assertThat(starredFlashcardRepository
                .existsByUser_UserIdAndFlashcard_FlashcardId(
                        user.getUserId(),
                        savedCard.getFlashcardId()))
                .isTrue();
    }

    @Test
    @DisplayName("unstarFlashcard - Should remove star relation for authenticated user")
    void unstarFlashcard_ShouldRemoveStarRelation() {
        // given
        User user = userRepository.findById(2L).orElseThrow();
        mockAuthenticatedUser(user);

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities.flashcardSetBuilder()
                        .setId(null)
                        .user(user)
                        .build());

        Flashcard savedCard = flashcardRepository.save(
                TestEntities.flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(savedSet)
                        .build());

        starredFlashcardRepository.save(TestEntities.createUserStarredFlashcard(user, savedCard));

        // when
        FlashcardDTO result = starredFlashcardService
                .unstarFlashcard(savedCard.getFlashcardId());

        // then
        assertThat(result.starred()).isFalse();

        assertThat(starredFlashcardRepository
                .existsByUser_UserIdAndFlashcard_FlashcardId(
                        user.getUserId(),
                        savedCard.getFlashcardId()))
                .isFalse();
    }

    @Test
    @DisplayName("getStarredFlashcardIdsForCurrentUserInSet - Should return only starred flashcards from requested set")
    void getStarredFlashcardIdsForCurrentUserInSet_ShouldReturnStarredIdsFromSet() {
        // given
        User user = userRepository.findById(2L).orElseThrow();
        mockAuthenticatedUser(user);

        FlashcardSet savedSet = flashcardSetRepository.save(
                TestEntities.flashcardSetBuilder()
                        .setId(null)
                        .user(user)
                        .build());

        Flashcard starredCard = flashcardRepository.save(
                TestEntities.flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(savedSet)
                        .build());

        Flashcard unstarredCard = flashcardRepository.save(
                TestEntities.flashcardBuilder()
                        .flashcardId(null)
                        .flashcardSet(savedSet)
                        .build());

        starredFlashcardRepository.save(
                TestEntities.createUserStarredFlashcard(user, starredCard));

        // when
        Set<Long> result = starredFlashcardService
                        .getStarredFlashcardIdsForCurrentUserInSet(savedSet.getSetId());

        // then
        assertThat(result)
                .containsExactly(starredCard.getFlashcardId())
                .doesNotContain(unstarredCard.getFlashcardId());
    }
}
