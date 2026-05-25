package vn.system.app.modules.evaluation.domain;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.evaluation.domain.enums.PeriodStatus;
import vn.system.app.modules.user.domain.User;

/**
 * Kỳ đánh giá HQCV.
 * Ví dụ: "Đánh giá HQCV năm 2025".
 *
 * Luồng trạng thái: DRAFT → ACTIVE → CLOSED.
 * Khi admin kích hoạt (ACTIVE), hệ thống tự động sinh evaluation_record cho mọi nhân viên.
 */
@Entity
@Table(name = "evaluation_periods")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EvaluationPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PeriodStatus status = PeriodStatus.DRAFT;

    // ── Mốc thời gian ──────────────────────────────────────────────────────────

    /** Ngày mở để nhân viên bắt đầu tự đánh giá */
    @Column(name = "employee_start_date")
    private Instant employeeStartDate;

    /** Deadline nhân viên phải nộp tự đánh giá */
    @Column(name = "employee_deadline")
    private Instant employeeDeadline;

    /** Deadline quản lý trực tiếp phải chấm xong */
    @Column(name = "manager_deadline")
    private Instant managerDeadline;

    /** Deadline quản lý gián tiếp phải phê duyệt xong */
    @Column(name = "approval_deadline")
    private Instant approvalDeadline;

    // ── Quan hệ ────────────────────────────────────────────────────────────────
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    @JsonIgnoreProperties({ "departments" })
    private vn.system.app.modules.company.domain.Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    @JsonIgnoreProperties({ "subordinates", "directManager", "userInfo", "role" })
    private User createdByUser;

    /** Các template được áp dụng trong kỳ này */
    @OneToMany(mappedBy = "period", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties("period")
    private List<PeriodTemplate> periodTemplates;

    /** Danh sách nhân viên tham gia kỳ này */
    @OneToMany(mappedBy = "period", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties("period")
    private List<PeriodEmployee> periodEmployees;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
