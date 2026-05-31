package com.turggut.sms.iam.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        UserInfo user) {

    public record UserInfo(Long id, String username, String email, String role, String fullName) {}
}
