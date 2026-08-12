package com.brainbooster.security.authorization;

import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.user.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OwnerOrAdminPolicyTest {

    private final OwnerOrAdminPolicy policy = new OwnerOrAdminPolicy();

    @Test
    void verify_ShouldAllowAccess_WhenUserIsOwner() {
        AuthenticatedUser user = new AuthenticatedUser(
                1L,
                Role.USER);

        assertThatCode(() -> policy.verify(user, 1L, "Access denied")
        ).doesNotThrowAnyException();
    }

    @Test
    void verify_ShouldAllowAccess_WhenUserIsAdmin() {
        AuthenticatedUser admin = new AuthenticatedUser(
                2L,
                Role.ADMIN);

        assertThatCode(() -> policy.verify(admin, 1L, "Access denied")
        ).doesNotThrowAnyException();
    }

    @Test
    void verify_ShouldThrowAccessDeniedException_WhenUserIsNeitherOwnerNorAdmin() {
        AuthenticatedUser user = new AuthenticatedUser(
                2L,
                Role.USER);

        assertThatThrownBy(() -> policy.verify(user, 1L, "Access denied")
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access denied");
    }
}
