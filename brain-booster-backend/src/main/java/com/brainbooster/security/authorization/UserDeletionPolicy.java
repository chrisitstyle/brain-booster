package com.brainbooster.security.authorization;

import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.user.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UserDeletionPolicy {

    private static final String ACCESS_DENIED_MESSAGE = "You cannot delete yourself or other users";

    public void verify(AuthenticatedUser authenticatedUser,
            Long targetUserId) {
        boolean isAdmin = Role.ADMIN.equals(authenticatedUser.role());

        boolean isSelf = Objects.equals(
                        authenticatedUser.userId(),
                        targetUserId);

        if (!isAdmin || isSelf) {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }
    }
}
