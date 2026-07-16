package vn.system.app.modules.usersession.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.system.app.modules.usersession.domain.UserSession;
import vn.system.app.modules.usersession.repository.UserSessionRepository;
import vn.system.app.modules.user.domain.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class UserSessionService {

    private static final int MAX_SESSIONS_PER_USER = 10;

    private final UserSessionRepository userSessionRepository;
    private final long refreshTokenValiditySeconds;

    public UserSessionService(
            UserSessionRepository userSessionRepository,
            @Value("${lotusgroup.jwt.refresh-token-validity-in-seconds}") long refreshTokenValiditySeconds) {
        this.userSessionRepository = userSessionRepository;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }

    @Transactional
    public void createSession(User user, String refreshToken, String userAgent, String ipAddress) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setRefreshToken(refreshToken);
        session.setRefreshTokenHash(hashToken(refreshToken));
        session.setExpiresAt(Instant.now().plus(refreshTokenValiditySeconds, ChronoUnit.SECONDS));
        session.setUserAgent(userAgent);
        session.setIpAddress(ipAddress);
        userSessionRepository.saveAndFlush(session);
        pruneOldSessions(user.getId());
    }

    public Optional<UserSession> findByRefreshToken(String refreshToken) {
        String tokenHash = hashToken(refreshToken);
        Optional<UserSession> indexedSession = userSessionRepository.findByRefreshTokenHash(tokenHash);
        if (indexedSession.isPresent()) {
            return indexedSession;
        }

        // One-time compatibility path for sessions created before the hash column existed.
        Optional<UserSession> legacySession = userSessionRepository.findByRefreshToken(refreshToken);
        legacySession.ifPresent(session -> {
            session.setRefreshTokenHash(tokenHash);
            if (session.getExpiresAt() == null) {
                Instant createdAt = session.getCreatedAt() != null ? session.getCreatedAt() : Instant.now();
                session.setExpiresAt(createdAt.plus(refreshTokenValiditySeconds, ChronoUnit.SECONDS));
            }
            userSessionRepository.save(session);
        });
        return legacySession;
    }

    @Transactional
    public void updateSessionToken(String oldRefreshToken, String newRefreshToken) {
        findByRefreshToken(oldRefreshToken).ifPresent(session -> {
            session.setRefreshToken(newRefreshToken);
            session.setRefreshTokenHash(hashToken(newRefreshToken));
            session.setExpiresAt(Instant.now().plus(refreshTokenValiditySeconds, ChronoUnit.SECONDS));
            userSessionRepository.save(session);
        });
    }

    @Transactional
    public void deleteSession(String refreshToken) {
        if (refreshToken != null) {
            long deleted = userSessionRepository.deleteByRefreshTokenHash(hashToken(refreshToken));
            if (deleted == 0) {
                userSessionRepository.deleteByRefreshToken(refreshToken);
            }
        }
    }

    @Transactional
    public void deleteAllSessionsForUser(String email) {
        userSessionRepository.deleteByUser_Email(email);
    }

    @Scheduled(cron = "0 30 2 * * ?", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void deleteExpiredSessions() {
        Instant now = Instant.now();
        Instant legacyCutoff = now.minus(refreshTokenValiditySeconds, ChronoUnit.SECONDS);
        userSessionRepository.deleteExpiredSessions(now, legacyCutoff);
    }

    private void pruneOldSessions(String userId) {
        List<Long> keepIds = userSessionRepository.findLatestSessionIds(
                userId,
                PageRequest.of(0, MAX_SESSIONS_PER_USER));
        if (keepIds != null && !keepIds.isEmpty()) {
            userSessionRepository.deleteSessionsExcept(userId, keepIds);
        }
    }

    private String hashToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Refresh token must not be blank");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
