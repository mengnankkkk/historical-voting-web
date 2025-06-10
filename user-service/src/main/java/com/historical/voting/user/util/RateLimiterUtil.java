package com.historical.voting.user.util;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterUtil {

    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    public RateLimiter resolveLimiter(String key, double permitsPerSecond) {
        return limiters.computeIfAbsent(key, k -> RateLimiter.create(permitsPerSecond));
    }

    public boolean tryAcquire(String key, double permitsPerSecond) {
        RateLimiter limiter = resolveLimiter(key, permitsPerSecond);
        return limiter.tryAcquire();
    }
}
