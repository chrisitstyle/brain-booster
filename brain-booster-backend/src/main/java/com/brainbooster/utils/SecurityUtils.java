package com.brainbooster.utils;

import com.brainbooster.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for handling security-related operations.
 * <p>
 * Provides helper methods to securely access the current authentication context
 * and retrieve the logged-in user details.
 */
public class SecurityUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private SecurityUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Retrieves the currently authenticated user from the Spring Security context.
     *
     * @return the currently authenticated {@link User}.
     * @throws AccessDeniedException if the user is not authenticated, is anonymous,
     *                               or if the principal is not an instance of {@link User}.
     */
    public static User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("User is not authenticated");
        }

        if (!(authentication.getPrincipal() instanceof User authUser)) {
            throw new AccessDeniedException("Invalid user principal");
        }

        return authUser;
    }
}
