package vn.system.app.modules.auth.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import vn.system.app.common.util.error.TooManyRequestsException;

class AuthLoginRateLimitServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-16T04:00:00Z"), ZoneOffset.UTC);

    @Test
    void blocksThirteenthAttemptForSameIpAndAccountWithinOneMinute() {
        AuthLoginRateLimitService service = new AuthLoginRateLimitService(clock);

        for (int attempt = 0; attempt < 12; attempt++) {
            assertDoesNotThrow(() -> service.checkAllowed("118.69.230.1", "user@example.com"));
        }

        assertThrows(
                TooManyRequestsException.class,
                () -> service.checkAllowed("118.69.230.1", "user@example.com"));
    }

    @Test
    void blocksIpAfterSixtyAttemptsAcrossDifferentAccounts() {
        AuthLoginRateLimitService service = new AuthLoginRateLimitService(clock);

        for (int attempt = 0; attempt < 60; attempt++) {
            int accountNumber = attempt;
            assertDoesNotThrow(() -> service.checkAllowed(
                    "118.69.230.1",
                    "user" + accountNumber + "@example.com"));
        }

        assertThrows(
                TooManyRequestsException.class,
                () -> service.checkAllowed("118.69.230.1", "another@example.com"));
    }
}
