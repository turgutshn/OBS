package com.turggut.sms.iam.service;

import com.turggut.sms.shared.config.AppProperties;
import com.turggut.sms.iam.domain.RefreshTokenRepository;
import com.turggut.sms.iam.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Auth security side-effects that must be persisted even when the surrounding
 * login/refresh transaction rolls back (because it ends by throwing). Each
 * method runs in its own committed transaction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthSecurityService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AppProperties appProperties;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerFailedAttempt(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            int threshold = appProperties.security().password().lockoutThreshold();
            int duration = appProperties.security().password().lockoutDurationMinutes();
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= threshold) {
                user.setLockedUntil(Instant.now().plus(duration, ChronoUnit.MINUTES));
                log.warn("User '{}' locked after {} failed attempts", user.getUsername(), user.getFailedAttempts());
            }
            userRepository.save(user);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.revokeAllForUser(userId);
    }
}
