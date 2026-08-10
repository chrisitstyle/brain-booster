package com.brainbooster.security.authorization;

import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class AdminPolicy {

    private static final String ADMIN_ACCESS_DENIED_MESSAGE = "Only admins can access this resource.";

    public void verify(User user) {
        if (user == null || !Role.ADMIN.equals(user.getRole())) {
            throw new AccessDeniedException(ADMIN_ACCESS_DENIED_MESSAGE);
        }
    }
}
