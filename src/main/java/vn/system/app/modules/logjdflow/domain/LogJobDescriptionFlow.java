package vn.system.app.modules.logjdflow.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;

@Entity
@Table(name = "log_job_description_flow")
@Getter
@Setter
public class LogJobDescriptionFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long jobDescriptionId;

    @Column(nullable = false)
    private Long flowId;

    // ❌ step đã xoá – vì hệ thống duyệt theo cấp bậc, không theo step
    // private Integer step;

    /**
     * SEND – Người tạo gửi duyệt
     * APPROVE – Duyệt & chuyển cấp
     * APPROVE_END – Duyệt và kết thúc duyệt (chờ ban hành)
     * REJECT – Từ chối
     * ISSUE – Ban hành JD
     */
    @Column(nullable = false)
    private String action;

    private Long fromUserId;
    private Long toUserId;

    @Column(columnDefinition = "TEXT")
    private String comment;

    private Instant createdAt;
    private String createdBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
