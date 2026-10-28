package com.banjaraghar.backend.common.ratelimit;

import com.banjaraghar.backend.exception.TooManyRequestsException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/** Simple in-memory limit: at most maxRequests per key inside a time window. */
@Service
public class RateLimitService {

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /** Counts one request for the key and throws once it has made more than maxRequests inside the window. */
    public void check(String key, int maxRequests, Duration window) {
        Instant now = Instant.now();
        Window current = windows.compute(key, (k, existing) ->
                existing == null || existing.isExpired(now)
                        ? new Window(now.plus(window), 1)
                        : new Window(existing.expiresAt, existing.count + 1));

        if (current.count > maxRequests) {
            throw new TooManyRequestsException("Too many requests. Please try again later.");
        }
    }

    // Keeps the map from growing forever
    @Scheduled(fixedRate = 600_000)
    public void removeExpired() {
        Instant now = Instant.now();
        windows.values().removeIf(window -> window.isExpired(now));
    }

    private static final class Window {
        private final Instant expiresAt;
        private final int count;

        private Window(Instant expiresAt, int count) {
            this.expiresAt = expiresAt;
            this.count = count;
        }

        private boolean isExpired(Instant now) {
            return expiresAt.isBefore(now);
        }
    }
}