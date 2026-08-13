package com.brainbooster.security.authorization;

import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.user.Role;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class UserDeletionPolicyTest {

    private final UserDeletionPolicy userDeletionPolicy = new UserDeletionPolicy();

    @Test
    void verify_ShouldAllowAdmin_WhenDeletingAnotherUser() {
        AuthenticatedUser admin = new AuthenticatedUser(1L, Role.ADMIN);

        Assertions.assertThatCode(() ->
                userDeletionPolicy.verify(admin, 2L)).doesNotThrowAnyException();
    }

    @Test
    void verify_ShouldThrowAccessDenied_WhenAdminDeletesSelf() {
        AuthenticatedUser admin = new AuthenticatedUser(1L, Role.ADMIN);

        Assertions.assertThatThrownBy(() ->
                        userDeletionPolicy.verify(admin, 1L)
                )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You cannot delete yourself or other users");
    }

    @Test
    void verify_ShouldThrowAccessDenied_WhenUserDeletesAnotherUser() {
        AuthenticatedUser user = new AuthenticatedUser(1L, Role.USER);

        Assertions.assertThatThrownBy(() ->
                        userDeletionPolicy.verify(user, 2L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void verify_ShouldThrowAccessDenied_WhenUserDeletesSelf() {
        AuthenticatedUser user = new AuthenticatedUser(1L, Role.USER);

        Assertions.assertThatThrownBy(() ->
                        userDeletionPolicy.verify(user, 1L))
                .isInstanceOf(AccessDeniedException.class);
    }
}
