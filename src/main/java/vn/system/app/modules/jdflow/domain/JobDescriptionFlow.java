package vn.system.app.modules.jdflow.domain;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;

@Entity
@Table(name = "job_description_flow")
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class JobDescriptionFlow {

    public enum FlowStatus {
        PENDING, // Đang chờ duyệt
        REJECTED, // Bị từ chối – có lý do
        WAITING_ISSUE, // Duyệt xong – chờ CEO ban hành
        DONE // CEO đã ban hành
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JD nào đang được duyệt
    @Column(nullable = false)
    private Long jobDescriptionId;

    // Ai gửi bước này
    @Column(nullable = false)
    private Long fromUserId;

    // Ai đang phải duyệt bước này
    private Long toUserId;

    // Lý do từ chối (nếu có)
    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlowStatus status;

    private Instant createdAt;
    private String createdBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");

        if (this.status == null) {
            this.status = FlowStatus.PENDING;
        }
    }
}