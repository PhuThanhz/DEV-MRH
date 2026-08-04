package vn.system.app.modules.jd.jobdescriptiontask.domain;

import java.time.Instant;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.common.util.SecurityUtil;

@Entity
@Table(name = "job_description_task_items")
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class JobDescriptionTaskItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * RELATION
     */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_description_task_id")
    @JsonIgnoreProperties({ "jobDescription", "items" })
    private JobDescriptionTask jobDescriptionTask;

    /*
     * ITEM INFO
     */

    private Integer orderNo;

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
