package vn.system.app.modules.permissionassignment.domain;

import jakarta.persistence.*;
import java.time.Instant;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.permissioncontent.domain.PermissionContent;
import vn.system.app.modules.departmentjobtitle.domain.DepartmentJobTitle;
import vn.system.app.modules.processaction.domain.ProcessAction;

@Entity
@Table(name = "permission_assignments")
public class PermissionAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_content_id", nullable = false)
    private PermissionContent permissionContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_job_title_id", nullable = false)
    private DepartmentJobTitle departmentJobTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_action_id", nullable = false)
    private ProcessAction processAction;

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

    // GETTERS & SETTERS
    public Long getId() {
        return id;
    }

    public PermissionContent getPermissionContent() {
        return permissionContent;
    }

    public void setPermissionContent(PermissionContent permissionContent) {
        this.permissionContent = permissionContent;
    }

    public DepartmentJobTitle getDepartmentJobTitle() {
        return departmentJobTitle;
    }

    public void setDepartmentJobTitle(DepartmentJobTitle departmentJobTitle) {
        this.departmentJobTitle = departmentJobTitle;
    }

    public ProcessAction getProcessAction() {
        return processAction;
    }

    public void setProcessAction(ProcessAction processAction) {
        this.processAction = processAction;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }
}
