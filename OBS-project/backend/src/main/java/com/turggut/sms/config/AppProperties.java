package com.turggut.sms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Security security, Bootstrap bootstrap) {

    public record Security(Jwt jwt, Cors cors, RateLimit rateLimit, Password password) {}

    public record Jwt(
            String issuer,
            int accessTokenTtlMinutes,
            int refreshTokenTtlDays,
            String secret) {}

    public record Cors(List<String> allowedOrigins) {}

    public record RateLimit(int authRequestsPerMinute, int apiRequestsPerMinute) {}

    public record Password(int minLength, int lockoutThreshold, int lockoutDurationMinutes) {}

    public record Bootstrap(String adminUsername, String adminPassword, String adminEmail) {}
}
