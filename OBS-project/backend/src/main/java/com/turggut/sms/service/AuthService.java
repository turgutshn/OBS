package com.turggut.sms.service;

import com.turggut.sms.config.AppProperties;
import com.turggut.sms.domain.student.StudentRepository;
import com.turggut.sms.domain.teacher.TeacherRepository;
import com.turggut.sms.domain.token.RefreshToken;
import com.turggut.sms.domain.token.RefreshTokenRepository;
import com.turggut.sms.domain.user.User;
import com.turggut.sms.domain.user.UserRepository;
import com.turggut.sms.dto.auth.AuthResponse;
import com.turggut.sms.dto.auth.LoginRequest;
import com.turggut.sms.dto.auth.PasswordChangeRequest;
import com.turggut.sms.exception.ApiException;
import com.turggut.sms.security.JwtTokenProvider;
import com.turggut.sms.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogService auditLogService;
    private final AppProperties appProperties;

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByUsername(request.username()).orElse(null);
        if (user == null) {
            auditLogService.failure("LOGIN", "User", request.username(), "user not found");
            throw ApiException.unauthorized("Invalid credentials");
        }

        if (user.isLocked()) {
            auditLogService.failure("LOGIN", "User", user.getUsername(), "account locked");
            throw ApiException.unauthorized("Account locked. Try again later.");
        }

        if (!user.isEnabled()) {
            auditLogService.failure("LOGIN", "User", user.getUsername(), "account disabled");
            throw ApiException.unauthorized("Account disabled");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            registerFailedAttempt(user);
            auditLogService.failure("LOGIN", "User", user.getUsername(), "wrong password");
            throw ApiException.unauthorized("Invalid credentials");
        }

        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String access = tokenProvider.generateAccessToken(user);
        String refresh = createRefreshToken(user, httpRequest);

        auditLogService.success("LOGIN", "User", user.getUsername(), "successful login");
        log.info("User '{}' logged in", user.getUsername());
        return buildResponse(user, access, refresh);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken, HttpServletRequest httpRequest) {
        String hash = hash(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> ApiException.unauthorized("Invalid refresh token"));

        if (!stored.isActive()) {
            refreshTokenRepository.revokeAllForUser(stored.getUser().getId());
            auditLogService.failure("REFRESH", "User", stored.getUser().getUsername(), "reused or expired token");
            throw ApiException.unauthorized("Refresh token no longer valid");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        String access = tokenProvider.generateAccessToken(user);
        String newRefresh = createRefreshToken(user, httpRequest);
        auditLogService.success("REFRESH", "User", user.getUsername(), null);
        return buildResponse(user, access, newRefresh);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        refreshTokenRepository.findByTokenHash(hash(refreshToken)).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            auditLogService.success("LOGOUT", "User", rt.getUser().getUsername(), null);
        });
    }

    @Transactional
    public void logoutAll() {
        Long userId = SecurityUtils.currentUserId();
        if (userId == null) return;
        int revoked = refreshTokenRepository.revokeAllForUser(userId);
        auditLogService.success("LOGOUT_ALL", "User", String.valueOf(userId), "revoked=" + revoked);
    }

    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        Long userId = SecurityUtils.currentUserId();
        if (userId == null) throw ApiException.unauthorized("Not authenticated");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("User not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            auditLogService.failure("PASSWORD_CHANGE", "User", user.getUsername(), "wrong current password");
            throw ApiException.badRequest("invalid_password", "Current password is incorrect");
        }
        validatePasswordStrength(request.newPassword());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllForUser(user.getId());
        auditLogService.success("PASSWORD_CHANGE", "User", user.getUsername(), null);
    }

    public AuthResponse currentUserInfo() {
        Long userId = SecurityUtils.currentUserId();
        if (userId == null) throw ApiException.unauthorized("Not authenticated");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("User not found"));
        return new AuthResponse(null, null, 0, buildUserInfo(user));
    }

    private void validatePasswordStrength(String pw) {
        int min = appProperties.security().password().minLength();
        if (pw.length() < min) {
            throw ApiException.badRequest("weak_password", "Password must be at least " + min + " characters");
        }
        boolean hasDigit = pw.chars().anyMatch(Character::isDigit);
        boolean hasLetter = pw.chars().anyMatch(Character::isLetter);
        if (!hasDigit || !hasLetter) {
            throw ApiException.badRequest("weak_password", "Password must contain letters and digits");
        }
    }

    private void registerFailedAttempt(User user) {
        int threshold = appProperties.security().password().lockoutThreshold();
        int duration = appProperties.security().password().lockoutDurationMinutes();
        user.setFailedAttempts(user.getFailedAttempts() + 1);
        if (user.getFailedAttempts() >= threshold) {
            user.setLockedUntil(Instant.now().plus(duration, ChronoUnit.MINUTES));
            log.warn("User '{}' locked after {} failed attempts", user.getUsername(), user.getFailedAttempts());
        }
        userRepository.save(user);
    }

    private String createRefreshToken(User user, HttpServletRequest httpRequest) {
        byte[] raw = new byte[48];
        RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        Instant expires = Instant.now().plus(appProperties.security().jwt().refreshTokenTtlDays(), ChronoUnit.DAYS);
        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(hash(token))
                .expiresAt(expires)
                .ipAddress(httpRequest != null ? clientIp(httpRequest) : null)
                .userAgent(httpRequest != null ? truncate(httpRequest.getHeader("User-Agent"), 255) : null)
                .build();
        refreshTokenRepository.save(entity);
        return token;
    }

    private AuthResponse buildResponse(User user, String access, String refresh) {
        long ttl = appProperties.security().jwt().accessTokenTtlMinutes() * 60L;
        return new AuthResponse(access, refresh, ttl, buildUserInfo(user));
    }

    private AuthResponse.UserInfo buildUserInfo(User user) {
        String fullName = switch (user.getRole()) {
            case STUDENT -> studentRepository.findByUserId(user.getId())
                    .map(s -> s.getFirstName() + " " + s.getLastName()).orElse(user.getUsername());
            case TEACHER -> teacherRepository.findByUserId(user.getId())
                    .map(t -> t.getFirstName() + " " + t.getLastName()).orElse(user.getUsername());
            case ADMIN -> "Administrator";
        };
        return new AuthResponse.UserInfo(user.getId(), user.getUsername(), user.getEmail(),
                user.getRole().name(), fullName);
    }

    private String hash(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(token.getBytes()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() > max ? value.substring(0, max) : value;
    }
}
