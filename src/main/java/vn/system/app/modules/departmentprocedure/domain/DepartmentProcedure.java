package vn.system.app.modules.departmentprocedure.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.common.util.SecurityUtil;
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
    @Column(name = "procedure_code", length = 100, nullable = false)
    private String procedureCode;
    private String procedureName;
    private String status;
    private Integer planYear;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String fileUrls;

    private String note;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Integer version = 1;

    // ===== RELATIONSHIP =====

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    @JsonIgnoreProperties({ "departmentProcedures" })
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    @JsonIgnoreProperties({ "departmentProcedures" })
    private Section section;

    // ===== AUDIT =====

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        if (!this.active)
            this.active = true;
        if (this.version == null)
            this.version = 1;
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}