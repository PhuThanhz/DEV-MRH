package vn.system.app.modules.positionlevel.domain;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;

@Entity
@Table(name = "position_levels")
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" }) // ⭐ FIX PROXY

public class PositionLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ví dụ: S1, M1, L3...
    @Column(unique = true, nullable = false)
    private String code; // mã cấp chức danh (S1, S2...)

    // Thứ tự ưu tiên band: S = 1, M = 2, L = 3...
    // Chỉ nhập khi tạo cấp đầu tiên của một band
    private Integer bandOrder;

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
