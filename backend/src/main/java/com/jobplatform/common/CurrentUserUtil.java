package com.jobplatform.common;

import com.jobplatform.security.UserDetailsAdapter;
import com.jobplatform.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUserUtil {

    private CurrentUserUtil() {
    }

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsAdapter userDetailsAdapter) {
            return userDetailsAdapter.getUser();
        }
        throw new IllegalStateException("Unexpected authentication principal type: " + principal.getClass());
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
