package com.brainbooster.security;

import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import static com.brainbooster.utils.TestEntities.createUser;
import static org.assertj.core.api.Assertions.assertThat;

class UserPrincipalTest {

    @Test
    void from_ShouldCreatePrincipalFromUser() {
        // given
        User user = createUser();

        // when
        UserPrincipal principal = UserPrincipal.from(user);

        // then
        assertThat(principal.userId()).isEqualTo(user.getUserId());

        assertThat(principal.getUsername()).isEqualTo(user.getEmail());

        assertThat(principal.getPassword()).isEqualTo(user.getPassword());

        assertThat(principal.role())
                .isEqualTo(user.getRole());
    }

    @Test
    void getAuthorities_ShouldReturnUserRole() {
        // given
        UserPrincipal principal = new UserPrincipal(
                1L,
                "johndoe@example.com",
                "encoded_password",
                Role.USER
        );

        // when, then
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(Role.USER.name());
    }

    @Test
    void getAuthorities_ShouldReturnAdminRole() {
        // given
        UserPrincipal principal = new UserPrincipal(
                1L,
                "admin@example.com",
                "encoded_password",
                Role.ADMIN
        );

        // when, then
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(Role.ADMIN.name());
    }
}
