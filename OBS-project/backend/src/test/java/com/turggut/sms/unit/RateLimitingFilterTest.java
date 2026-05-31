package com.turggut.sms.unit;

import com.turggut.sms.shared.config.AppProperties;
import com.turggut.sms.shared.security.RateLimitingFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RateLimitingFilterTest {

    private AppProperties props(int authLimit, int apiLimit) {
        return new AppProperties(
                new AppProperties.Security(
                        null, null,
                        new AppProperties.RateLimit(authLimit, apiLimit),
                        null),
                null);
    }

    private MockHttpServletRequest req(String uri, String ip) {
        MockHttpServletRequest r = new MockHttpServletRequest();
        r.setRequestURI(uri);
        r.setRemoteAddr(ip);
        return r;
    }

    @Test
    void allowsUpToLimitThenBlocks() throws Exception {
        RateLimitingFilter filter = new RateLimitingFilter(props(100, 2));

        MockFilterChain chain1 = new MockFilterChain();
        filter.doFilter(req("/api/courses", "9.9.9.9"), new MockHttpServletResponse(), chain1);
        assertNotNull(chain1.getRequest(), "1st request should pass");

        MockFilterChain chain2 = new MockFilterChain();
        filter.doFilter(req("/api/courses", "9.9.9.9"), new MockHttpServletResponse(), chain2);
        assertNotNull(chain2.getRequest(), "2nd request should pass");

        MockFilterChain chain3 = new MockFilterChain();
        MockHttpServletResponse blocked = new MockHttpServletResponse();
        filter.doFilter(req("/api/courses", "9.9.9.9"), blocked, chain3);
        assertNull(chain3.getRequest(), "3rd request should be blocked");
        assertEquals(429, blocked.getStatus());
    }

    @Test
    void authPathUsesAuthLimit() throws Exception {
        RateLimitingFilter filter = new RateLimitingFilter(props(1, 100));
        MockFilterChain first = new MockFilterChain();
        filter.doFilter(req("/api/auth/login", "8.8.8.8"), new MockHttpServletResponse(), first);
        assertNotNull(first.getRequest());

        MockFilterChain second = new MockFilterChain();
        MockHttpServletResponse blocked = new MockHttpServletResponse();
        filter.doFilter(req("/api/auth/login", "8.8.8.8"), blocked, second);
        assertNull(second.getRequest());
        assertEquals(429, blocked.getStatus());
    }

    @Test
    void nonApiPathBypassesLimiter() throws Exception {
        RateLimitingFilter filter = new RateLimitingFilter(props(1, 1));
        for (int i = 0; i < 5; i++) {
            MockFilterChain chain = new MockFilterChain();
            filter.doFilter(req("/actuator/health", "7.7.7.7"), new MockHttpServletResponse(), chain);
            assertNotNull(chain.getRequest());
        }
    }

    @Test
    void forwardedForHeaderIdentifiesClient() throws Exception {
        RateLimitingFilter filter = new RateLimitingFilter(props(100, 1));
        MockHttpServletRequest r = req("/api/courses", "0.0.0.0");
        r.addHeader("X-Forwarded-For", "203.0.113.5, 10.0.0.1");
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(r, new MockHttpServletResponse(), chain);
        assertNotNull(chain.getRequest());
    }
}
