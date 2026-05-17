package vn.system.app.modules.document.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "document_access")
@Getter
@Setter
public class DocumentAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "assigned_by")
    private String assignedBy;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "read_at")
    private Instant readAt;
}