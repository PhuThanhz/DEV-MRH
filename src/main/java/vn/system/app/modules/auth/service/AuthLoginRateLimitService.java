package vn.system.app.modules.auth.service;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import vn.system.app.common.util.error.TooManyRequestsException;

@Service
public class AuthLoginRateLimitService {

    private static final long WINDOW_MS = 60_000;
    private static final int MAX_ATTEMPTS_PER_IP = 60;
    private static final int MAX_ATTEMPTS_PER_ACCOUNT = 12;
    private static final long CLEANUP_INTERVAL_MS = 5 * 60_000;

    private final Clock clock;
    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();
    private volatile long lastCleanupAt = 0;

    public AuthLoginRateLimitService() {
        this(Clock.systemUTC());
    }

    AuthLoginRateLimitService(Clock clock) {
        this.clock = clock;
    }

    public void checkAllowed(String ip, String username) {
        long now = clock.millis();
        cleanupStaleWindows(now);

        String normalizedIp = normalize(ip);
        String normalizedUsername = normalize(username);

        checkKey("ip:" + normalizedIp, MAX_ATTEMPTS_PER_IP, now);
        checkKey("account:" + normalizedIp + ":" + normalizedUsername, MAX_ATTEMPTS_PER_ACCOUNT, now);
    }

    private void checkKey(String key, int maxAttempts, long now) {
        AttemptWindow window = attempts.computeIfAbsent(key, ignored -> new AttemptWindow());

        if (!window.tryAcquire(now, maxAttempts)) {
            throw new TooManyRequestsException("Bạn thử đăng nhập quá nhiều lần. Vui lòng đợi ít phút rồi thử lại.");
        }
    }

    private void cleanupStaleWindows(long now) {
        if (now - lastCleanupAt < CLEANUP_INTERVAL_MS) {
            return;
        }

        lastCleanupAt = now;
        attempts.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static final class AttemptWindow {
        private final Deque<Long> timestamps = new ArrayDeque<>();

        synchronized boolean tryAcquire(long now, int maxAttempts) {
            removeExpired(now);
            if (timestamps.size() >= maxAttempts) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }

        synchronized boolean isExpired(long now) {
            removeExpired(now);
            return timestamps.isEmpty();
        }

        private void removeExpired(long now) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
                timestamps.removeFirst();
            }
        }
    }
}
