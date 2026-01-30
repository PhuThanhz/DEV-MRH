package vn.system.app.modules.permissioncategoryscope.domain;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.permissioncategory.domain.PermissionCategory;

@Entity
@Table(name = "permission_category_scopes")
@Getter
@Setter
public class PermissionCategoryScope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * =========================
     * DANH MỤC QUYỀN
     * =========================
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "permission_category_id", nullable = false)
    private PermissionCategory category;

    /*
     * =========================
     * CHỨC DANH THUỘC PHÒNG BAN
     * =========================
     */
    private Long departmentJobTitleId;

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
