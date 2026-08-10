package com.brainbooster.security.authorization;

import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OwnerOrAdminPolicyTest {

    private final OwnerOrAdminPolicy policy = new OwnerOrAdminPolicy();

    @Test
    void verify_ShouldAllowAccess_WhenUserIsOwner() {
        User user = User.builder()
                .userId(1L)
                .role(Role.USER)
                .build();

        assertThatCode(() ->
                policy.verify(user, 1L, "Access denied")
        ).doesNotThrowAnyException();
    }

    @Test
    void verify_ShouldAllowAccess_WhenUserIsAdmin() {
        User admin = User.builder()
                .userId(2L)
                .role(Role.ADMIN)
                .build();

        assertThatCode(() ->
                policy.verify(admin, 1L, "Access denied")
        ).doesNotThrowAnyException();
    }

    @Test
    void verify_ShouldThrowAccessDeniedException_WhenUserIsNeitherOwnerNorAdmin() {
        User user = User.builder()
                .userId(2L)
                .role(Role.USER)
                .build();

        assertThatThrownBy(() -> policy.verify(user, 1L, "Access denied")
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access denied");
    }
}
