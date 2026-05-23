package com.turggut.sms.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<AuthenticatedUser> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof AuthenticatedUser user)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public static Long currentUserId() {
        return currentUser().map(AuthenticatedUser::getId).orElse(null);
    }

    public static String currentUsername() {
        return currentUser().map(AuthenticatedUser::getUsername).orElse("anonymous");
    }
}
