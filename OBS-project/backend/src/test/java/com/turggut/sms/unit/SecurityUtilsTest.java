package com.turggut.sms.unit;

import com.turggut.sms.iam.domain.Role;
import com.turggut.sms.iam.domain.User;
import com.turggut.sms.shared.security.AuthenticatedUser;
import com.turggut.sms.shared.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityUtilsTest {

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noAuthenticationYieldsAnonymous() {
        SecurityContextHolder.clearContext();
        assertTrue(SecurityUtils.currentUser().isEmpty());
        assertNull(SecurityUtils.currentUserId());
        assertEquals("anonymous", SecurityUtils.currentUsername());
    }

    @Test
    void authenticatedPrincipalIsResolved() {
        User user = User.builder().id(7L).username("bob").role(Role.ADMIN).enabled(true).build();
        AuthenticatedUser principal = new AuthenticatedUser(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        assertEquals(7L, SecurityUtils.currentUserId());
        assertEquals("bob", SecurityUtils.currentUsername());
        assertTrue(SecurityUtils.currentUser().isPresent());
    }
}
