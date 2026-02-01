package vn.system.app.modules.careerpath.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.jobtitle.domain.JobTitle;

@Entity
@Table(name = "career_paths")
@Getter
@Setter
public class CareerPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * ==========================
     * Liên kết
     * ==========================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_title_id", nullable = false)
    private JobTitle jobTitle;

    /*
     * ==========================
     * Các trường nhập text
     * ==========================
     */
    // Tiêu chuẩn chức danh
    @Column(columnDefinition = "TEXT")
    private String jobStandard;

    // Yêu cầu đào tạo
    @Column(columnDefinition = "TEXT")
    private String trainingRequirement;

    // Phương pháp đánh giá
    @Column(columnDefinition = "TEXT")
    private String evaluationMethod;

    // Thời gian giữ vị trí
    @Column(columnDefinition = "TEXT")
    private String requiredTime;

    // Kết quả đào tạo
    @Column(columnDefinition = "TEXT")
    private String trainingOutcome;

    // Hiệu quả công việc
    @Column(columnDefinition = "TEXT")
    private String performanceRequirement;

    // Salary
    @Column(columnDefinition = "TEXT")
    private String salaryNote;

    /*
     * ==========================
     * Trạng thái & audit
     * ==========================
     */
    private Integer status;
    private boolean active = true;

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
