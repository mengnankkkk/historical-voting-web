package com.historical.voting.user.util;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterUtil {

    // 使用正确的 Bucket 类型
    private final Map<String, Bucket> bucketMap = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key, int permitsPerSecond) {
        return bucketMap.computeIfAbsent(key, k -> {
            Bandwidth limit = Bandwidth.classic(permitsPerSecond, Refill.greedy(permitsPerSecond, Duration.ofSeconds(1)));
            return Bucket4j.builder().addLimit(limit).build();
        });
    }
}
