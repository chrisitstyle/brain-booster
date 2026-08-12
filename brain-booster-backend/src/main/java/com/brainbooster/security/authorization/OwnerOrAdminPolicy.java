package com.brainbooster.security.authorization;

import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.user.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class OwnerOrAdminPolicy {

    public void verify(AuthenticatedUser authenticatedUser, Long ownerId, String errorMessage) {
        boolean isAdmin = Role.ADMIN.equals(authenticatedUser.role());
        boolean isOwner = ownerId.equals(authenticatedUser.userId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(errorMessage);
        }
    }
}
