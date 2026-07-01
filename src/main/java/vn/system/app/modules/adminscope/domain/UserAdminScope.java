package vn.system.app.modules.adminscope.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.company.domain.Company;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.user.domain.User;

@Entity
@Table(name = "user_admin_scopes", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "scope_type", "company_id", "department_id" })
})
@Getter
@Setter
public class UserAdminScope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "scope_type", nullable = false, length = 30)
    private String scopeType; // COMPANY / DEPARTMENT

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false)
    private boolean active = true;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        Instant now = Instant.now();
        this.active = true;
        this.createdAt = now;
        this.updatedAt = now;
        String currentUser = SecurityUtil.getCurrentUserLogin().orElse("");
        this.createdBy = currentUser;
        this.updatedBy = currentUser;
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
