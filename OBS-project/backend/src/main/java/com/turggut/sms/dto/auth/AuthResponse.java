package com.turggut.sms.dto.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        UserInfo user) {

    public record UserInfo(Long id, String username, String email, String role, String fullName) {}
}
