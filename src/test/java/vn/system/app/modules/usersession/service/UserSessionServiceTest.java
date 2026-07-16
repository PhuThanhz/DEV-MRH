package vn.system.app.modules.usersession.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.usersession.domain.UserSession;
import vn.system.app.modules.usersession.repository.UserSessionRepository;

@ExtendWith(MockitoExtension.class)
class UserSessionServiceTest {

    private static final long TOKEN_VALIDITY_SECONDS = 3_600;

    @Mock
    private UserSessionRepository repository;

    private UserSessionService service;

    @BeforeEach
    void setUp() {
        service = new UserSessionService(repository, TOKEN_VALIDITY_SECONDS);
    }

    @Test
    void createsSessionWithIndexedTokenHashAndExpiry() {
        User user = new User();
        user.setId("user-1");

        service.createSession(user, "refresh-token", "browser", "127.0.0.1");

        ArgumentCaptor<UserSession> captor = ArgumentCaptor.forClass(UserSession.class);
        verify(repository).saveAndFlush(captor.capture());
        UserSession saved = captor.getValue();

        assertEquals(64, saved.getRefreshTokenHash().length());
        assertNotNull(saved.getExpiresAt());
        assertTrue(saved.getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void usesIndexedHashLookupForCurrentSessions() {
        UserSession session = new UserSession();
        when(repository.findByRefreshTokenHash(anyString())).thenReturn(Optional.of(session));

        Optional<UserSession> result = service.findByRefreshToken("refresh-token");

        assertTrue(result.isPresent());
        verify(repository, never()).findByRefreshToken("refresh-token");
    }

    @Test
    void backfillsHashForLegacySession() {
        UserSession session = new UserSession();
        session.setCreatedAt(Instant.now());
        when(repository.findByRefreshTokenHash(anyString())).thenReturn(Optional.empty());
        when(repository.findByRefreshToken("legacy-token")).thenReturn(Optional.of(session));

        Optional<UserSession> result = service.findByRefreshToken("legacy-token");

        assertTrue(result.isPresent());
        assertEquals(64, session.getRefreshTokenHash().length());
        assertNotNull(session.getExpiresAt());
        verify(repository).save(session);
    }
}
