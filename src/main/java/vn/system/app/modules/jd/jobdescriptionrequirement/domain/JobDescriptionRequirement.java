package vn.system.app.modules.jd.jobdescriptionrequirement.domain;

import java.time.Instant;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.jd.jobdescription.domain.JobDescription;

@Entity
@Table(name = "job_description_requirements")
@Getter
@Setter
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

    /*
     * REQUIREMENTS
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