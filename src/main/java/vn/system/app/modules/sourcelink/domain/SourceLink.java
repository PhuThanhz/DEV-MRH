package vn.system.app.modules.sourcelink.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;

import java.time.Instant;

@Entity
@Table(name = "source_links")
@Getter
@Setter
public class SourceLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @JsonIgnore
    private SourceGroup group;

    private String name;
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String caption;

    @Column(columnDefinition = "TEXT")
    private String contentGenerated;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProcessingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ContentType type = ContentType.VIDEO;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
        createdBy = SecurityUtil.getCurrentUserLogin().orElse("system");
        if (type == null)
            type = ContentType.VIDEO;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }

    public enum ProcessingStatus {
        SUCCESS, FAILED
    }

    public enum ContentType {
        IMAGE, VIDEO, TEXT, UNKNOWN
    }

    @JsonIgnore
    public boolean isPendingOrFailed() {
        return status == null
                || status == ProcessingStatus.FAILED
                || contentGenerated == null
                || contentGenerated.isBlank();
    }
}
