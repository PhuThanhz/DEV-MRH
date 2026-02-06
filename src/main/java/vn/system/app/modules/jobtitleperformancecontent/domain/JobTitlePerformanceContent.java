package vn.system.app.modules.jobtitleperformancecontent.domain;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;

@Entity
@Table(name = "job_title_performance_content")
@Getter
@Setter
public class JobTitlePerformanceContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OwnerLevel ownerLevel;

    private Long ownerJobTitleId;

    private Long salaryGradeId;

    /** ⭐ Số thứ tự của bậc lương (1,2,3,4...) */
    @Column(name = "salary_grade_number")
    private Integer salaryGradeNumber;

    @Column(columnDefinition = "TEXT")
    private String contentA;

    @Column(columnDefinition = "TEXT")
    private String contentB;

    @Column(columnDefinition = "TEXT")
    private String contentC;

    @Column(columnDefinition = "TEXT")
    private String contentD;

    private Boolean active = true;

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
