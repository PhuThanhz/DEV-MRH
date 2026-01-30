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
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" }) // ⭐ FIX PROXY

public class JobDescriptionFlow {

    // ================= ENUM TRẠNG THÁI FLOW ================= //
    public enum FlowStatus {
        PENDING, // Đang chờ duyệt
        REJECTED, // Bị từ chối – trả về người trước
        WAITING_ISSUE, // Đã duyệt xong – chờ ban hành
        DONE // Đã ban hành
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JD nào đang được duyệt
    @Column(nullable = false)
    private Long jobDescriptionId;

    // Người gửi duyệt
    @Column(nullable = false)
    private Long fromUserId;

    // Người đang được giao duyệt (null khi chờ CEO)
    @Column
    private Long toUserId;

    // ❌ step đã bị loại bỏ — duyệt theo cấp bậc User
    // private Integer step;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlowStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");

        if (this.status == null) {
            this.status = FlowStatus.PENDING;
        }
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
