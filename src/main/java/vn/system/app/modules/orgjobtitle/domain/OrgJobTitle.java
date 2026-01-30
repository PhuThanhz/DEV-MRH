package vn.system.app.modules.orgjobtitle.domain;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.jobtitle.domain.JobTitle;
import vn.system.app.modules.company.domain.Company;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.section.domain.Section;

@Entity
@Table(name = "org_job_titles", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "job_title_id", "company_id" }),
        @UniqueConstraint(columnNames = { "job_title_id", "department_id" }),
        @UniqueConstraint(columnNames = { "job_title_id", "section_id" })
}, indexes = {
        @Index(name = "idx_org_job_title_job_title", columnList = "job_title_id"),
        @Index(name = "idx_org_job_title_company", columnList = "company_id"),
        @Index(name = "idx_org_job_title_department", columnList = "department_id"),
        @Index(name = "idx_org_job_title_section", columnList = "section_id")
})
@Getter
@Setter
public class OrgJobTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * =========================
     * CHỨC DANH
     * =========================
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_title_id", nullable = false)
    private JobTitle jobTitle;

    /*
     * =========================
     * TỔ CHỨC (CHỈ 1 TRONG 3)
     * =========================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    /*
     * =========================
     * TRẠNG THÁI
     * =========================
     */
    // 1 = active, 0 = inactive
    private Integer status;

    /*
     * =========================
     * AUDIT
     * =========================
     */
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.status = this.status == null ? 1 : this.status;
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
