package vn.system.app.modules.position.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.company.domain.Company;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.section.domain.Section;
import vn.system.app.modules.jobtitle.domain.JobTitle;

@Entity
@Table(name = "positions")
@Getter
@Setter
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =======================================
    // RELATIONS
    // =======================================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_title_id")
    private JobTitle jobTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reports_to")
    private Position reportsTo;

    private boolean active = true;

    // =======================================
    // AUDIT
    // =======================================
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    protected void beforeCreate() {
        Instant now = Instant.now();
        this.active = true;

        this.createdAt = now;
        this.updatedAt = now;

        String user = SecurityUtil.getCurrentUserLogin().orElse("");
        this.createdBy = user;
        this.updatedBy = user;
    }

    @PreUpdate
    protected void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}