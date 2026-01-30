package vn.system.app.modules.permissionassignment.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.permissioncontent.domain.PermissionContent;
import vn.system.app.modules.departmentjobtitle.domain.DepartmentJobTitle;
import vn.system.app.modules.processaction.domain.ProcessAction;

@Entity
@Table(name = "permission_assignments")
@Getter
@Setter
public class PermissionAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "permission_content_id", nullable = false)
    private PermissionContent permissionContent;

    @ManyToOne
    @JoinColumn(name = "department_job_title_id", nullable = false)
    private DepartmentJobTitle departmentJobTitle;

    @ManyToOne
    @JoinColumn(name = "process_action_id", nullable = false)
    private ProcessAction processAction;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
