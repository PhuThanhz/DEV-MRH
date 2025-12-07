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

    // ============================================
    // Liên kết ngược ManyToOne -> tránh vòng lặp JSON
    // ============================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @JsonIgnore // tránh vòng lặp group <-> links
    private SourceGroup group;

    // ==== Thông tin người đăng ====
    private String name;
    private String userId;

    // ==== Nội dung bài ====
    @Column(columnDefinition = "TEXT")
    private String caption; // Mô tả bài đăng

    @Column(columnDefinition = "TEXT")
    private String contentGenerated; // Tên file/video đã tải về

    @Column(columnDefinition = "TEXT")
    private String errorMessage; // Lưu lỗi khi xử lý

    // ==== Trạng thái xử lý ====
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProcessingStatus status; // SUCCESS / FAILED

    // ==== Loại nội dung ====
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ContentType type = ContentType.VIDEO;

    // ==== Audit fields ====
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    // ==== Lifecycle hooks ====
    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.createdAt = Instant.now();
        if (this.type == null) {
            this.type = ContentType.VIDEO;
        }
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.updatedAt = Instant.now();
    }

    // ==== Enum định nghĩa trạng thái xử lý ====
    public enum ProcessingStatus {
        SUCCESS,
        FAILED
    }

    // ==== Enum loại nội dung ====
    public enum ContentType {
        IMAGE,
        VIDEO,
        TEXT,
        UNKNOWN
    }

    @JsonIgnore
    public boolean isPendingOrFailed() {
        return this.status == null
                || this.status == ProcessingStatus.FAILED
                || this.contentGenerated == null
                || this.contentGenerated.isBlank();
    }
}
