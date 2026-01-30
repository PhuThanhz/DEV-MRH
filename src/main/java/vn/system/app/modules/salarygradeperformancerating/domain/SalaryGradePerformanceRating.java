package vn.system.app.modules.salarygradeperformancerating.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.salarygrade.domain.SalaryGrade;

@Entity
@Table(name = "salary_grade_performance_rating", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "salary_grade_id" })
})
@Getter
@Setter
public class SalaryGradePerformanceRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "salary_grade_id", nullable = false)
    private SalaryGrade salaryGrade;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String ratingAText;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String ratingBText;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String ratingCText;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String ratingDText;

    private Integer status;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.status = this.status == null ? 1 : this.status;
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.updatedAt = Instant.now();
    }
}
