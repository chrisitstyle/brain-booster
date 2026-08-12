package com.brainbooster.security.authorization;

import com.brainbooster.security.AuthenticatedUser;
import com.brainbooster.user.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class AdminPolicy {

    private static final String ADMIN_ACCESS_DENIED_MESSAGE = "Only admins can access this resource.";

    public void verify(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || !Role.ADMIN.equals(authenticatedUser.role())) {
            throw new AccessDeniedException(ADMIN_ACCESS_DENIED_MESSAGE);
        }
    }
}
