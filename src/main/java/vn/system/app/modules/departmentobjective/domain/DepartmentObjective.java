package vn.system.app.modules.departmentobjective.domain;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.section.domain.Section;

@Entity
@Table(name = "department_objectives")
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class DepartmentObjective {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * OBJECTIVE | TASK
     */
    @NotBlank(message = "Type không được để trống")
    private String type;

    @NotBlank(message = "Content không được để trống")
    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer orderNo;

    private LocalDate issueDate;

    /*
     * DEPARTMENT
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties({ "company" })
    private Department department;

    /*
     * SECTION (optional)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    @JsonIgnoreProperties({ "department" })
    private Section section;

    private Integer status;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {

        this.status = this.status == null ? 1 : this.status;

        if (this.issueDate == null) {
            this.issueDate = LocalDate.now();
        }

        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}