package com.brainbooster.security;

import com.brainbooster.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Provides access to the currently authenticated user.
 * <p>
 * Reads authentication data from the Spring Security context and exposes
 * methods for retrieving the current user either strictly or optionally.
 */
@Component
public class CurrentUserProvider {

    /**
     * Retrieves the currently authenticated user from the Spring Security context.
     *
     * @return the currently authenticated {@link User}.
     * @throws AccessDeniedException if the user is not authenticated, is anonymous,
     *  or if the principal is not an instance of {@link User}.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (
                authentication == null ||
                        !authentication.isAuthenticated() ||
                        "anonymousUser".equals(authentication.getPrincipal())
        ) {
            throw new AccessDeniedException("User is not authenticated");
        }

        if (!(authentication.getPrincipal() instanceof User authUser)) {
            throw new AccessDeniedException("Invalid user principal");
        }

        return authUser;
    }

    /**
     * Retrieves the currently authenticated user from the Spring Security context,
     * or returns {@code null} when no valid authenticated user is available.
     * <p>
     * This method does not throw an exception for anonymous, unauthenticated,
     * or invalid principals.
     *
     * @return the currently authenticated {@link User}, or {@code null}
     * if no valid user is authenticated.
     */
    public User getCurrentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (
                authentication == null ||
                        !authentication.isAuthenticated() ||
                        "anonymousUser".equals(authentication.getPrincipal())
        ) {
            return null;
        }

        if (!(authentication.getPrincipal() instanceof User authUser)) {
            return null;
        }

        return authUser;
    }
}
