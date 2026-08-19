package com.abrar.BOOKSTORE.Login.jwt;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Lets a JWT be revoked before its natural expiry - e.g. on logout - instead
// of staying valid for its full 24h lifetime regardless of session state.
// Keyed by the token's "jti" claim (see JwtTokenProvider), not the raw token
// itself, so revoking a token doesn't require storing the token text.
//
// In-memory only, same tradeoff as RateLimiter: fine for a single instance,
// resets on restart (which is harmless here - a restarted app has no
// in-flight sessions to protect), and would need a shared store (Redis/DB)
// behind a load balancer with multiple instances.
@Component
public class TokenBlacklist {

    private final Map<String, Instant> revokedUntil = new ConcurrentHashMap<>();

    /**
     * Revokes the token with this jti until its own expiry - after that it
     * would be rejected as expired anyway, so there's no need to remember it
     * any longer.
     */
    public void revoke(String jti, Instant tokenExpiry) {
        if (jti == null) {
            return;
        }
        revokedUntil.put(jti, tokenExpiry);
    }

    public boolean isRevoked(String jti) {
        return jti != null && revokedUntil.containsKey(jti);
    }

    // Mirrors RateLimiter.evictStaleEntries(): without this, every logout
    // leaves a permanent entry behind even once the underlying token would
    // have expired naturally anyway - a slow memory leak over time.
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void evictExpiredEntries() {
        Instant now = Instant.now();
        revokedUntil.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}