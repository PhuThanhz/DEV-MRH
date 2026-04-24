package vn.system.app.modules.sharetoken.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "procedure_share_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcedureShareToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "procedure_id", nullable = false)
    private Long procedureId;

    @Column(name = "procedure_type", nullable = false, length = 20)
    private String procedureType; // "COMPANY" | "DEPARTMENT" | "CONFIDENTIAL"

    @Column(name = "token", nullable = false, length = 36, unique = true)
    private String token;

    @Column(name = "pin", length = 6)
    private String pin; // nullable

    @Column(name = "permission", nullable = false, length = 20)
    private String permission; // "VIEW_INFO" | "VIEW_FILE" | "VIEW_ALL"

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "max_access_count")
    private Integer maxAccessCount; // nullable = không giới hạn

    @Column(name = "access_count")
    private Integer accessCount;

    @Column(name = "qr_code", columnDefinition = "MEDIUMTEXT")
    private String qrCode;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "is_revoked")
    private Boolean isRevoked;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.accessCount = 0;
        this.isRevoked = false;
    }
}