package vn.system.app.modules.permissioncategory.domain;

import java.time.Instant;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.department.domain.Department;

@Entity
@Table(name = "permission_categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = "code")
})
@Getter
@Setter
public class PermissionCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "code không được để trống")
    @Column(nullable = false)
    private String code;

    @NotBlank(message = "name không được để trống")
    @Column(nullable = false)
    private String name;

    /*
     * ==========================
     * DEPARTMENT (N-1)
     * ==========================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    @NotNull(message = "departmentId không được để trống")
    private Department department;

    /*
     * ==========================
     * ACTIVE (SOFT DELETE)
     * ==========================
     */
    @Column(nullable = false)
    private Boolean active = true;

    /*
     * ==========================
     * AUDIT
     * ==========================
     */
    @Column(updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @Column(updatable = false)
    private String createdBy;

    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;

        String user = SecurityUtil.getCurrentUserLogin().orElse("");
        this.createdBy = user;
        this.updatedBy = user;

        if (this.active == null) {
            this.active = true;
        }
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
