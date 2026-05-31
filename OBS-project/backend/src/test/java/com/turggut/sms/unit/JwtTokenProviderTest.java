package com.turggut.sms.unit;

import com.turggut.sms.config.AppProperties;
import com.turggut.sms.domain.user.Role;
import com.turggut.sms.domain.user.User;
import com.turggut.sms.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private static final String VALID_SECRET =
            "VGVzdFNlY3JldEtleVRoYXRJc0xvbmdFbm91Z2hUb1NhdGlzZnlSZXF1aXJlbWVudHNGb3JKV1RIUw==";

    private AppProperties props(String secret) {
        return new AppProperties(
                new AppProperties.Security(
                        new AppProperties.Jwt("turggut-sms", 15, 7, secret),
                        null, null, null),
                null);
    }

    private User user() {
        return User.builder().id(42L).username("alice").email("a@x.io").role(Role.TEACHER).build();
    }

    @Test
    void generatesAndParsesToken() {
        JwtTokenProvider provider = new JwtTokenProvider(props(VALID_SECRET));
        String token = provider.generateAccessToken(user());
        assertTrue(provider.isValid(token));
        Claims claims = provider.parse(token);
        assertEquals("42", claims.getSubject());
        assertEquals("alice", claims.get("username"));
        assertEquals("TEACHER", claims.get("role"));
        assertEquals("turggut-sms", claims.getIssuer());
    }

    @Test
    void invalidTokenIsRejected() {
        JwtTokenProvider provider = new JwtTokenProvider(props(VALID_SECRET));
        assertFalse(provider.isValid("not.a.jwt"));
        assertFalse(provider.isValid(""));
    }

    @Test
    void shortSecretIsRejectedAtConstruction() {
        String shortSecret = Base64.getEncoder().encodeToString("too-short".getBytes());
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider(props(shortSecret)));
    }
}
