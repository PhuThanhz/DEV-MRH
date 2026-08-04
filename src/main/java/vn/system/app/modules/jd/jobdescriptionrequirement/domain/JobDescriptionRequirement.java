package vn.system.app.modules.jd.jobdescriptionrequirement.domain;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.BatchSize;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.jd.jobdescription.domain.JobDescription;

@Entity
@Table(name = "job_description_requirements")
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class JobDescriptionRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * RELATION
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_description_id", nullable = false)
    private JobDescription jobDescription;

    @OneToMany(mappedBy = "jobDescriptionRequirement", fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<JobDescriptionRequirementItem> items;

    /*
     * REQUIREMENTS
     * Các cột string cũ dưới đây không còn dùng để đọc/ghi — nội dung nay nằm ở
     * {@link #items} (mỗi dòng 1 record, phân loại theo category). Giữ lại cột vì
     * ddl-auto=update không tự drop; dữ liệu cũ đã tách sang
     * job_description_requirement_items bởi JobDescriptionDataMigrationRunner.
     */
    @Column(columnDefinition = "TEXT")
    private String knowledge;

    @Column(columnDefinition = "TEXT")
    private String experience;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String qualities;

    @Column(columnDefinition = "TEXT")
    private String otherRequirements;

    /*
     * AUDIT
     */
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