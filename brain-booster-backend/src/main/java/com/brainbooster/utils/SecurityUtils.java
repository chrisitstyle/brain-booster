package com.brainbooster.utils;

import com.brainbooster.user.Role;
import com.brainbooster.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for handling security-related operations.
 * <p>
 * Provides helper methods to safely access the current Spring Security context,
 * retrieve the logged-in user, and perform common authorization checks.
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
     * Unlike {@link #getAuthenticatedUser()}, this method does not throw an exception
     * for anonymous, unauthenticated, or invalid principals. It is useful for endpoints
     * that can be accessed both anonymously and by logged-in users.
     *
     * @return the currently authenticated {@link User}, or {@code null} if no valid user is authenticated.
     */
    public static User getAuthenticatedUserOrNull() {
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

    /**
     * Checks whether the given user has administrator privileges.
     *
     * @param user the user to check. May be {@code null}.
     * @return {@code true} if the user is not {@code null} and has the {@link Role#ADMIN} role;
     * otherwise {@code false}.
     */
    public static boolean isAdmin(User user) {
        return user != null && Role.ADMIN.equals(user.getRole());
    }

    /**
     * Verifies that the given user has administrator privileges.
     *
     * @param user the user to verify.
     * @throws AccessDeniedException if the user is {@code null} or does not have the {@link Role#ADMIN} role.
     */
    public static void verifyAdmin(User user) {
        if (!isAdmin(user)) {
            throw new AccessDeniedException("Only admins can access this resource.");
        }
    }

    /**
     * Verifies that the currently authenticated user has administrator privileges.
     * <p>
     * This method first retrieves the authenticated user from the security context
     * using {@link #getAuthenticatedUser()}, then checks whether that user has
     * the {@link Role#ADMIN} role.
     *
     * @throws AccessDeniedException if the current user is not authenticated or does not have administrator privileges.
     */
    public static void verifyAdmin() {
        verifyAdmin(getAuthenticatedUser());
    }
}