package vn.system.app.modules.salarygrade.domain;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.orgjobtitle.domain.OrgJobTitle;
import jakarta.persistence.Column;

@Entity
@Table(name = "salary_grades", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "org_job_title_id", "grade_level" })
}, indexes = {
        @Index(name = "idx_salary_grade_org_job_title", columnList = "org_job_title_id"),
        @Index(name = "idx_salary_grade_status", columnList = "status")
})
@Getter
@Setter
public class SalaryGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * ===============================
     * NGỮ CẢNH CHỨC DANH
     * (Company / Department / Section)
     * ===============================
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_job_title_id", nullable = false)
    private OrgJobTitle orgJobTitle;

    /*
     * ===============================
     * BẬC LƯƠNG
     * 1, 2, 3, ...
     * ===============================
     */
    @Column(name = "grade_level", nullable = false)
    private Integer gradeLevel;

    /*
     * ===============================
     * TRẠNG THÁI
     * 1 = active, 0 = inactive
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

    /*
     * ===============================
     * LIFECYCLE
     * ===============================
     */
    @PrePersist
    public void handleBeforeCreate() {
        this.status = this.status == null ? 1 : this.status;
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
