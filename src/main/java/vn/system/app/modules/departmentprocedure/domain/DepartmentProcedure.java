package vn.system.app.modules.departmentprocedure.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.company.domain.Company;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.section.domain.Section;

@Entity
@Table(name = "department_procedure")
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class DepartmentProcedure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String procedureName;

    private String status;

    private Integer planYear;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String fileUrl;

    private String note;

    private boolean active;

    /*
     * ==========================
     * RELATIONSHIP
     * ==========================
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    @JsonIgnoreProperties({ "departmentProcedures" })
    private Company company;

    @Column(name = "company_id", insertable = false, updatable = false)
    private Long companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties({ "departmentProcedures" })
    private Department department;

    @Column(name = "department_id", insertable = false, updatable = false)
    private Long departmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    @JsonIgnoreProperties({ "departmentProcedures" })
    private Section section;

    @Column(name = "section_id", insertable = false, updatable = false)
    private Long sectionId;

    /*
     * ==========================
     * AUDIT
     * ==========================
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