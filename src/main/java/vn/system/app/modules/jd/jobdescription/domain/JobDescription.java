package vn.system.app.modules.jd.jobdescription.domain;

import java.time.Instant;

import jakarta.persistence.*;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.BatchSize;
import java.util.List;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.company.domain.Company;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.companyjobtitle.domain.CompanyJobTitle;
import vn.system.app.modules.departmentjobtitle.domain.DepartmentJobTitle;
import vn.system.app.modules.sectionjobtitle.domain.SectionJobTitle;
import vn.system.app.modules.jd.jobdescriptionrequirement.domain.JobDescriptionRequirement;
import vn.system.app.modules.jd.jobdescriptiontask.domain.JobDescriptionTask;
import vn.system.app.modules.jd.jobdescriptionposition.domain.JobDescriptionPosition;
import vn.system.app.modules.jd.jdflow.domain.JdFlowLog;

@Entity
@Table(name = "job_descriptions")
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class JobDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * ==========================
     * RELATION
     * ==========================
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    /*
     * ==========================
     * JOB TITLE
     * ==========================
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_job_title_id")
    private CompanyJobTitle companyJobTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_job_title_id")
    private DepartmentJobTitle departmentJobTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_job_title_id")
    private SectionJobTitle sectionJobTitle;

    @OneToOne(mappedBy = "jobDescription", fetch = FetchType.LAZY)
    private JobDescriptionRequirement requirement;

    @OneToMany(mappedBy = "jobDescription", fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<JobDescriptionTask> tasks;

    @OneToMany(mappedBy = "jobDescription", fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<JobDescriptionPosition> positions;

    @OneToMany(mappedBy = "jobDescription", fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<JdFlowLog> logs;

    /*
     * ==========================
     * BASIC JD INFO
     * ==========================
     */

    private String code;

    @Column(columnDefinition = "TEXT")
    private String reportTo;

    @Column(columnDefinition = "TEXT")
    private String belongsTo;

    @Column(columnDefinition = "TEXT")
    private String collaborateWith;

    /*
     * ==========================
     * STATUS
     * ==========================
     */

    @Column(length = 20)
    private String status = "DRAFT";

    @Version
    private Integer version;

    private Instant effectiveDate;

    /*
     * ==========================
     * AUDIT
     * ==========================
     */

    private Instant createdAt;

    private Instant updatedAt;

    @Column(updatable = false)
    private String createdBy;

    private String updatedBy;

    /*
     * ==========================
     * JPA AUDIT
     * ==========================
     */

    @PrePersist
    public void beforeCreate() {

        this.createdAt = Instant.now();

        if (this.version == null) {
            this.version = 1;
        }

        if (this.status == null) {
            this.status = "DRAFT";
        }

        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }

    @PreUpdate
    public void beforeUpdate() {

        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}