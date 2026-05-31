package com.turggut.sms.service;

import com.turggut.sms.domain.audit.AuditLog;
import com.turggut.sms.domain.audit.AuditLogRepository;
import com.turggut.sms.security.AuthenticatedUser;
import com.turggut.sms.security.RequestIdFilter;
import com.turggut.sms.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void record(String action, String entityType, String entityId, String status, String details) {
        try {
            AuthenticatedUser user = SecurityUtils.currentUser().orElse(null);
            HttpServletRequest req = currentRequest();
            AuditLog entry = AuditLog.builder()
                    .occurredAt(Instant.now())
                    .actorUsername(user != null ? user.getUsername() : "anonymous")
                    .actorRole(user != null ? user.getRole().name() : null)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .ipAddress(req != null ? clientIp(req) : null)
                    .userAgent(req != null ? truncate(req.getHeader("User-Agent"), 255) : null)
                    .requestId(MDC.get(RequestIdFilter.MDC_KEY))
                    .status(status)
                    .details(truncate(details, 4000))
                    .build();
            repository.save(entry);
            log.info("AUDIT action={} entity={}:{} actor={} status={}",
                    action, entityType, entityId, entry.getActorUsername(), status);
        } catch (Exception ex) {
            log.warn("Failed to persist audit log for action={}: {}", action, ex.getMessage());
        }
    }

    public void success(String action, String entityType, String entityId, String details) {
        record(action, entityType, entityId, "SUCCESS", details);
    }

    public void failure(String action, String entityType, String entityId, String details) {
        record(action, entityType, entityId, "FAILURE", details);
    }

    private HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest();
        }
        return null;
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
