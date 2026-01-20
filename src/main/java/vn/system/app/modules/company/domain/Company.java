package vn.system.app.modules.company.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;

@Entity
@Table(name = "companies")
@Getter
@Setter
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // ================= THÔNG TIN CÔNG TY =================

    @NotBlank(message = "Mã công ty không được để trống")
    @Column(unique = true)
    private String code; // mã công ty

    @NotBlank(message = "Tên công ty không được để trống")
    private String name; // tên công ty (tiếng Việt)

    private String englishName; // tên công ty tiếng Anh

    /**
     * Trạng thái công ty
     * 1 = ACTIVE
     * 0 = INACTIVE (không xoá)
     */
    private Integer status;

    // ================= AUDIT FIELDS =================

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    // ================= JPA LIFECYCLE =================

    @PrePersist
    public void handleBeforeCreate() {
        this.status = 1; // mặc định ACTIVE

        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        this.updatedAt = Instant.now();
    }
}
