package vn.system.app.modules.usersession.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.system.app.modules.usersession.domain.UserSession;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);
    Optional<UserSession> findByRefreshToken(String refreshToken);
    long deleteByRefreshTokenHash(String refreshTokenHash);
    void deleteByRefreshToken(String refreshToken);
    void deleteByUser_Email(String email);

    @Query("SELECT s.id FROM UserSession s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<Long> findLatestSessionIds(@Param("userId") String userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.user.id = :userId AND s.id NOT IN :keepIds")
    int deleteSessionsExcept(
            @Param("userId") String userId,
            @Param("keepIds") List<Long> keepIds);

    @Modifying
    @Query("DELETE FROM UserSession s " +
            "WHERE (s.expiresAt IS NOT NULL AND s.expiresAt < :now) " +
            "OR (s.expiresAt IS NULL AND s.createdAt < :legacyCutoff)")
    int deleteExpiredSessions(
            @Param("now") Instant now,
            @Param("legacyCutoff") Instant legacyCutoff);
}
