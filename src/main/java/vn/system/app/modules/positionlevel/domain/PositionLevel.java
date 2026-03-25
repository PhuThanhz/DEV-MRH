package vn.system.app.modules.positionlevel.domain;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.company.domain.Company; // ⭐ import Company

@Entity
@Table(name = "position_levels", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "code", "company_id" }) // ⭐ unique theo cặp
})
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class PositionLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code; // S1, S2, M1... — unique constraint chuyển lên @Table

    private Integer bandOrder;

    private Integer status = 1; // 1 = active, 0 = inactive

    // ⭐ THÊM MỚI — liên kết công ty
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        this.status = this.status == null ? 1 : this.status;
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}