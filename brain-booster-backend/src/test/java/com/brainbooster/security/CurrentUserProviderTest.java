package com.brainbooster.security;

import com.brainbooster.user.User;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentUserProviderTest {

    private final CurrentUserProvider currentUserProvider = new CurrentUserProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_ShouldReturnAuthenticatedUser() {
        // given
        User user = TestEntities.createUser();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        User result = currentUserProvider.getCurrentUser();

        // then
        assertThat(result).isEqualTo(user);
    }

    @Test
    void getCurrentUser_ShouldThrowAccessDeniedException_WhenAuthenticationIsMissing() {
        // when + then
        assertThatThrownBy(currentUserProvider::getCurrentUser)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("User is not authenticated");
    }

    @Test
    void getCurrentUser_ShouldThrowAccessDeniedException_WhenUserIsAnonymous() {
        // given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                        "anonymousUser",
                        null,
                        List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when + then
        assertThatThrownBy(currentUserProvider::getCurrentUser)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("User is not authenticated");
    }

    @Test
    void getCurrentUser_ShouldThrowAccessDeniedException_WhenPrincipalIsInvalid() {
        // given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                        "invalidPrincipal",
                        null,
                        List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when + then
        assertThatThrownBy(currentUserProvider::getCurrentUser)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Invalid user principal");
    }

    @Test
    void getCurrentUserOrNull_ShouldReturnAuthenticatedUser() {
        // given
        User user = TestEntities.createUser();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        User result = currentUserProvider.getCurrentUserOrNull();

        // then
        assertThat(result).isEqualTo(user);
    }

    @Test
    void getCurrentUserOrNull_ShouldReturnNull_WhenAuthenticationIsMissing() {
        // when
        User result = currentUserProvider.getCurrentUserOrNull();

        // then
        assertThat(result).isNull();
    }

    @Test
    void getCurrentUserOrNull_ShouldReturnNull_WhenUserIsAnonymous() {
        // given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                        "anonymousUser",
                        null,
                        List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        User result = currentUserProvider.getCurrentUserOrNull();

        // then
        assertThat(result).isNull();
    }

    @Test
    void getCurrentUserOrNull_ShouldReturnNull_WhenPrincipalIsInvalid() {
        // given
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "invalidPrincipal",
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        User result = currentUserProvider.getCurrentUserOrNull();

        // then
        assertThat(result).isNull();
    }
}
