package vn.system.app.modules.jd.jobdescriptiontask.domain;

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
@Table(name = "job_description_tasks")
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
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

    @OneToMany(mappedBy = "jobDescriptionTask", fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<JobDescriptionTaskItem> items;

    /*
     * TASK INFO
     */

    private Integer orderNo;

    private String title;

    /**
     * Cột cũ, không còn dùng để đọc/ghi — nội dung mục con nay nằm ở {@link #items}.
     * Giữ lại cột (không xoá) vì ddl-auto=update không tự drop; dữ liệu cũ đã được
     * tách sang job_description_task_items bởi JobDescriptionDataMigrationRunner.
     */
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