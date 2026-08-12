package com.brainbooster.security.authorization;

import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.user.Role;
import com.brainbooster.utils.TestEntities;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminPolicyTest {

    private final AdminPolicy adminPolicy = new AdminPolicy();

    @Test
    void verify_ShouldAllowAdmin() {
        // given
        AuthenticatedUser admin = TestEntities.createAuthenticatedUser(1L, Role.ADMIN);

        // when, then
        assertThatCode(() -> adminPolicy.verify(admin))
                .doesNotThrowAnyException();
    }

    @Test
    void verify_ShouldThrowAccessDeniedException_WhenUserIsNotAdmin() {
        // given
        AuthenticatedUser user = TestEntities.createAuthenticatedUser(1L, Role.USER);

        // when, then
        assertThatThrownBy(() -> adminPolicy.verify(user))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only admins can access this resource.");
    }

    @Test
    void verify_ShouldThrowAccessDeniedException_WhenUserIsNull() {
        // when, then
        assertThatThrownBy(() -> adminPolicy.verify(null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only admins can access this resource.");
    }
}
