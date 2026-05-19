package vn.system.app.modules.evaluation.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.modules.evaluation.domain.enums.RecordStatus;
import vn.system.app.modules.user.domain.User;

/**
 * Bảng trung tâm của module HQCV — một bản đánh giá cá nhân.
 *
 * Sinh tự động khi admin kích hoạt kỳ đánh giá.
 * Trạng thái ban đầu: NOT_STARTED → tự chuyển sang EMPLOYEE_DRAFTING.
 *
 * Luồng trạng thái đầy đủ:
 *   NOT_STARTED
 *   → EMPLOYEE_DRAFTING      (kỳ được kích hoạt, nhân viên bắt đầu điền)
 *   → PENDING_MANAGER_REVIEW (nhân viên nộp)
 *   → MANAGER_REVIEWING      (quản lý trực tiếp lưu nháp)
 *   → PENDING_APPROVAL       (quản lý trực tiếp gửi phê duyệt)
 *   → REVISION_NEEDED        (quản lý gián tiếp trả lại)
 *   → PENDING_APPROVAL       (quản lý trực tiếp sửa và gửi lại)
 *   → COMPLETED              (quản lý gián tiếp phê duyệt)
 *
 * RULE xếp loại (dựa trên tổng điểm sau trọng số):
 *   < 3.0  → E
 *   3.0–3.5 → D
 *   3.5–4.0 → C
 *   4.0–4.5 → B
 *   > 4.5  → A
 */
@Entity
@Table(name = "evaluation_records",
        uniqueConstraints = @UniqueConstraint(columnNames = { "period_id", "employee_id" }))
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EvaluationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    @JsonIgnoreProperties({ "periodTemplates", "periodEmployees" })
    private EvaluationPeriod period;

    /** Nhân viên được đánh giá */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({ "subordinates", "directManager", "userInfo", "role" })
    private User employee;

    /** Quản lý trực tiếp — người chấm điểm Giai đoạn 2 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direct_manager_id", nullable = false)
    @JsonIgnoreProperties({ "subordinates", "directManager", "userInfo", "role" })
    private User directManager;

    /** Quản lý gián tiếp — người phê duyệt Giai đoạn 3 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indirect_manager_id", nullable = false)
    @JsonIgnoreProperties({ "subordinates", "directManager", "userInfo", "role" })
    private User indirectManager;

    /** Template form được dùng cho bản đánh giá này */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    @JsonIgnoreProperties("sections")
    private EvaluationTemplate template;

    // ── Trạng thái luồng ───────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RecordStatus status = RecordStatus.NOT_STARTED;

    // ── Các mốc thời gian ─────────────────────────────────────────────────────

    /** Ngày nhân viên nộp tự đánh giá */
    @Column(name = "employee_submitted_at")
    private Instant employeeSubmittedAt;

    /** Ngày quản lý trực tiếp nộp kết quả chấm */
    @Column(name = "manager_submitted_at")
    private Instant managerSubmittedAt;

    /** Ngày quản lý gián tiếp phê duyệt */
    @Column(name = "approved_at")
    private Instant approvedAt;

    /** Ngày hoàn thành (sau khi nhân viên xác nhận đã xem) */
    @Column(name = "completed_at")
    private Instant completedAt;

    // ── Kết quả ────────────────────────────────────────────────────────────────

    /** Tổng điểm sau khi nhân viên tự chấm (tính tự động) */
    @Column(name = "employee_total_score")
    private Double employeeTotalScore;

    /** Tổng điểm sau khi quản lý trực tiếp chấm (tính tự động) */
    @Column(name = "manager_total_score")
    private Double managerTotalScore;

    /**
     * Xếp loại cuối: A / B / C / D / E
     * Tính theo managerTotalScore khi record được COMPLETED.
     */
    @Column(name = "final_grade", length = 1)
    private String finalGrade;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
    }
}
