package vn.system.app.modules.usersession.domain;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.user.domain.User;

@Entity
@Table(
        name = "user_sessions",
        indexes = {
                @Index(name = "ux_user_sessions_refresh_token_hash", columnList = "refresh_token_hash", unique = true),
                @Index(name = "ix_user_sessions_expires_at", columnList = "expires_at")
        })
@Getter
@Setter
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String refreshToken;

    @Column(name = "refresh_token_hash", length = 64)
    private String refreshTokenHash;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
