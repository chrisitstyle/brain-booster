package com.brainbooster.security;

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
     * @return the authenticated user identity.
     * @throws AccessDeniedException if the user is not authenticated, is anonymous,
     *                               or the principal is not a valid {@link UserPrincipal}.
     */
    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (
                authentication == null ||
                        !authentication.isAuthenticated() ||
                        "anonymousUser".equals(authentication.getPrincipal())
        ) {
            throw new AccessDeniedException("User is not authenticated");
        }

        if (!(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("Invalid user principal");
        }

        return new AuthenticatedUser(
                principal.userId(),
                principal.role()
        );
    }

    /**
     * Retrieves the currently authenticated user from the Spring Security context,
     * or returns {@code null} when no valid authenticated user is available.
     * <p>
     * This method does not throw an exception for anonymous, unauthenticated,
     * or invalid principals.
     *
     * @return the authenticated user identity, or {@code null}
     * if no valid user is authenticated.
     */
    public AuthenticatedUser getCurrentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (
                authentication == null ||
                        !authentication.isAuthenticated() ||
                        "anonymousUser".equals(authentication.getPrincipal())
        ) {
            return null;
        }

        if (!(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return null;
        }

        return new AuthenticatedUser(
                principal.userId(),
                principal.role());
    }
}
