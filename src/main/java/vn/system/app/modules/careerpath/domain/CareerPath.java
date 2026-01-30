package vn.system.app.modules.careerpath.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.jobtitle.domain.JobTitle;

@Entity
@Table(name = "career_paths", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "department_id", "job_title_id" })
})
@Getter
@Setter
public class CareerPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // phòng ban
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    // chức danh
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_title_id", nullable = false)
    private JobTitle jobTitle;

    // tiêu chuẩn chức danh
    @Column(columnDefinition = "TEXT")
    private String jobStandard;

    // yêu cầu đào tạo
    @Column(columnDefinition = "TEXT")
    private String trainingRequirement;

    // phương pháp đánh giá
    @Column(columnDefinition = "TEXT")
    private String evaluationMethod;

    // thời gian giữ vị trí
    @Column(columnDefinition = "TEXT")
    private String requiredTime;

    // kết quả đào tạo
    @Column(columnDefinition = "TEXT")
    private String trainingOutcome;

    // hiệu quả công việc
    @Column(columnDefinition = "TEXT")
    private String performanceRequirement;

    // 🔹 DÒNG LƯƠNG (bạn nói bị thiếu)
    @Column(columnDefinition = "TEXT")
    private String salaryNote;

    // trạng thái
    private Integer status;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        this.status = this.status == null ? 1 : this.status;
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
