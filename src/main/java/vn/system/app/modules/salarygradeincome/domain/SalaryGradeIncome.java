package vn.system.app.modules.salarygradeincome.domain;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.salarygrade.domain.SalaryGrade;

@Entity
@Table(name = "salary_grade_incomes", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "salary_grade_id", "pay_type" })
})
@Getter
@Setter
public class SalaryGradeIncome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * ===============================
     * BẬC LƯƠNG
     * ===============================
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salary_grade_id", nullable = false)
    private SalaryGrade salaryGrade;

    /*
     * ===============================
     * HÌNH THỨC TÍNH
     * ===============================
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type", nullable = false)
    private PayType payType;

    /*
     * ===============================
     * CƠ CẤU THU NHẬP (SỐ)
     * ===============================
     */
    private BigDecimal baseSalary;
    private BigDecimal positionAllowance;
    private BigDecimal mealAllowance;
    private BigDecimal fuelAllowance;
    private BigDecimal phoneAllowance;
    private BigDecimal otherAllowance;

    /*
     * ===============================
     * THƯỞNG HIỆU QUẢ (A/B/C/D)
     * ===============================
     */
    private BigDecimal bonusA;
    private BigDecimal bonusB;
    private BigDecimal bonusC;
    private BigDecimal bonusD;

    /*
     * ===============================
     * TRẠNG THÁI
     * ===============================
     */
    private Integer status;

    /*
     * ===============================
     * AUDIT
     * ===============================
     */
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.status = this.status == null ? 1 : this.status;
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
