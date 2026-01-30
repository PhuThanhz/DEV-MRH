package vn.system.app.modules.processaction.domain;

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
@Table(name = "process_actions")
@Getter
@Setter
public class ProcessAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // XD, RS, TĐ, PD, TH, KS, TB
    @NotBlank(message = "code không được để trống")
    @Column(unique = true)
    private String code;

    // Xây dựng, Rà soát...
    @NotBlank(message = "name không được để trống")
    private String name;

    // Giải thích tên đầu mục
    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    // Định nghĩa chi tiết
    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    private boolean isActive = true;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.updatedAt = Instant.now();
    }
}
