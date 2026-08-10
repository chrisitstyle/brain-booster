package com.brainbooster.security.authorization;

import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class OwnerOrAdminPolicy {

    public void verify(User authenticatedUser, Long ownerId, String errorMessage) {
        boolean isAdmin = Role.ADMIN.equals(authenticatedUser.getRole());
        boolean isOwner = ownerId.equals(authenticatedUser.getUserId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(errorMessage);
        }
    }
}
