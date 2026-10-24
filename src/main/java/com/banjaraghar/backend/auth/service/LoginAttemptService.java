package com.banjaraghar.backend.auth.service;

import com.banjaraghar.backend.exception.TooManyRequestsException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, Attempts> attempts = new ConcurrentHashMap<>();

    public void checkAllowed(String key) {
        Attempts current = attempts.get(key);
        if (current != null && !current.isExpired() && current.count >= MAX_FAILURES) {
            throw new TooManyRequestsException("Too many failed login attempts. Please try again in a few minutes.");
        }
    }

    public void recordFailure(String key) {
        attempts.compute(key, (k, current) ->
                current == null || current.isExpired() ? new Attempts(Instant.now(), 1) : new Attempts(current.windowStart, current.count + 1));
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    // Keeps the map from growing forever
    @Scheduled(fixedRate = 600_000)
    public void removeExpired() {
        attempts.values().removeIf(Attempts::isExpired);
    }

    private static final class Attempts {
        private final Instant windowStart;
        private final int count;

        private Attempts(Instant windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }

        private boolean isExpired() {
            return windowStart.plus(WINDOW).isBefore(Instant.now());
        }
    }
}
