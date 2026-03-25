package vn.system.app.modules.jd.jobdescriptiontask.domain;

import java.time.Instant;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.jd.jobdescription.domain.JobDescription;

@Entity
@Table(name = "job_description_tasks")
@Getter
@Setter
public class JobDescriptionTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * RELATION
     */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_description_id")
    private JobDescription jobDescription;

    /*
     * TASK INFO
     */

    private Integer orderNo;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

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