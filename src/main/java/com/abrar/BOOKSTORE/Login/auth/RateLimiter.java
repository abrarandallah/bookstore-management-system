package com.abrar.BOOKSTORE.Login.auth;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

// Keeps /forgot-password from being usable to mass-check which emails/usernames
// are registered: caps each IP to a handful of attempts per window. In-memory
// only (resets on restart, not shared across instances) - fine for a single
// small deployment; swap for Redis/DB-backed limiting if this ever runs behind
// a load balancer with multiple instances.
@Component
public class RateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 15 * 60;

    private final ConcurrentHashMap<String, Deque<Instant>> attemptsByKey = new ConcurrentHashMap<>();

    public boolean allow(String key) {
        Instant now = Instant.now();
        Deque<Instant> attempts = attemptsByKey.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        synchronized (attempts) {
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(now.minusSeconds(WINDOW_SECONDS))) {
                attempts.pollFirst();
            }
            if (attempts.size() >= MAX_ATTEMPTS) {
                return false;
            }
            attempts.addLast(now);
            return true;
        }
    }

    // Without this, every distinct IP that ever calls allow() gets a
    // permanent entry in the map, even once its attempts have all aged out -
    // a slow leak over weeks/months of uptime. Runs hourly and just drops
    // keys whose deque is empty after trimming expired attempts.
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void evictStaleEntries() {
        Instant cutoff = Instant.now().minusSeconds(WINDOW_SECONDS);
        attemptsByKey.forEach((key, attempts) -> {
            synchronized (attempts) {
                while (!attempts.isEmpty() && attempts.peekFirst().isBefore(cutoff)) {
                    attempts.pollFirst();
                }
                if (attempts.isEmpty()) {
                    attemptsByKey.remove(key, attempts);
                }
            }
        });
    }
}