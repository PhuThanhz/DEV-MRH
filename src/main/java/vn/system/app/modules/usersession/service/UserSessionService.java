package vn.system.app.modules.usersession.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.system.app.modules.usersession.domain.UserSession;
import vn.system.app.modules.usersession.repository.UserSessionRepository;
import vn.system.app.modules.user.domain.User;

import java.util.Optional;

@Service
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;

    public UserSessionService(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    @Transactional
    public void createSession(User user, String refreshToken, String userAgent, String ipAddress) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setRefreshToken(refreshToken);
        session.setUserAgent(userAgent);
        session.setIpAddress(ipAddress);
        userSessionRepository.save(session);
    }

    public Optional<UserSession> findByRefreshToken(String refreshToken) {
        return userSessionRepository.findByRefreshToken(refreshToken);
    }

    @Transactional
    public void updateSessionToken(String oldRefreshToken, String newRefreshToken) {
        userSessionRepository.findByRefreshToken(oldRefreshToken).ifPresent(session -> {
            session.setRefreshToken(newRefreshToken);
            userSessionRepository.save(session);
        });
    }

    @Transactional
    public void deleteSession(String refreshToken) {
        if (refreshToken != null) {
            userSessionRepository.deleteByRefreshToken(refreshToken);
        }
    }

    @Transactional
    public void deleteAllSessionsForUser(String email) {
        userSessionRepository.deleteByUser_Email(email);
    }
}
