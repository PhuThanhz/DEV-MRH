package vn.system.app.modules.evaluation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.modules.evaluation.domain.enums.PeriodEmployeeStatus;
import vn.system.app.modules.user.domain.User;

/**
 * Danh sách nhân viên được add vào một kỳ đánh giá.
 * Admin có thể add từng người hoặc bulk theo phòng ban.
 *
 * Mỗi bản ghi xác định rõ:
 *  - employee: nhân viên tham gia đánh giá
 *  - directManager: quản lý trực tiếp sẽ chấm điểm (Giai đoạn 2)
 *  - indirectManager: quản lý gián tiếp sẽ phê duyệt (Giai đoạn 3)
 *  - template: form mẫu sẽ dùng cho nhân viên này
 *
 * RULE: Khi nhân viên nghỉ giữa kỳ, chuyển status = CANCELLED (không xóa dữ liệu).
 */
@Entity
@Table(name = "period_employees",
        uniqueConstraints = @UniqueConstraint(columnNames = { "period_id", "employee_id" }))
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PeriodEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    @JsonIgnoreProperties("periodEmployees")
    private EvaluationPeriod period;

    /** Nhân viên tham gia đánh giá */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({ "subordinates", "directManager", "userInfo", "role" })
    private User employee;

    /**
     * Quản lý trực tiếp của nhân viên trong kỳ này.
     * Người sẽ chấm điểm (Giai đoạn 2).
     * Có thể khác với directManager mặc định trong bảng users nếu admin override.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direct_manager_id", nullable = false)
    @JsonIgnoreProperties({ "subordinates", "directManager", "userInfo", "role" })
    private User directManager;

    /**
     * Quản lý gián tiếp của nhân viên trong kỳ này.
     * Người sẽ phê duyệt kết quả (Giai đoạn 3).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indirect_manager_id", nullable = false)
    @JsonIgnoreProperties({ "subordinates", "directManager", "userInfo", "role" })
    private User indirectManager;

    /** Template form sẽ dùng cho nhân viên này trong kỳ */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    @JsonIgnoreProperties("sections")
    private EvaluationTemplate template;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PeriodEmployeeStatus status = PeriodEmployeeStatus.ACTIVE;
}
