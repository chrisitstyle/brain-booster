package com.brainbooster.security;

import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record UserPrincipal(
        Long userId,
        String email,
        String passwordHash,
        Role role
) implements UserDetails {

    public static UserPrincipal from(User user) {
        return new UserPrincipal(
                user.getUserId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole());
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(role.name())
        );
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    @NonNull
    public String getUsername() {
        return email;
    }
}
