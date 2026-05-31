package com.turggut.sms.shared.security;

import com.turggut.sms.shared.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple sliding-window per-IP rate limiter for the in-process API.
 * Tracks timestamps within a 1-minute window. Adequate for a single-instance
 * deployment; for multi-instance setups replace with a Redis-backed limiter.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000L;

    private final Map<String, Deque<Long>> authWindows = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> apiWindows = new ConcurrentHashMap<>();
    private final int authLimit;
    private final int apiLimit;

    public RateLimitingFilter(AppProperties props) {
        this.authLimit = props.security().rateLimit().authRequestsPerMinute();
        this.apiLimit = props.security().rateLimit().apiRequestsPerMinute();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String clientKey = resolveClientKey(request);

        boolean allowed;
        if (path.startsWith("/api/auth/")) {
            allowed = tryAcquire(authWindows, clientKey, authLimit);
        } else if (path.startsWith("/api/")) {
            allowed = tryAcquire(apiWindows, clientKey, apiLimit);
        } else {
            chain.doFilter(request, response);
            return;
        }

        if (allowed) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"rate_limit_exceeded\",\"message\":\"Too many requests\"}");
        }
    }

    private boolean tryAcquire(Map<String, Deque<Long>> windows, String key, int limit) {
        long now = System.currentTimeMillis();
        Deque<Long> window = windows.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (window) {
            while (!window.isEmpty() && now - window.peekFirst() > WINDOW_MS) {
                window.pollFirst();
            }
            if (window.size() >= limit) {
                return false;
            }
            window.addLast(now);
            return true;
        }
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
