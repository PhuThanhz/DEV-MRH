package vn.system.app.modules.jobtitle.domain;

import java.time.Instant;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.positionlevel.domain.PositionLevel;

@Entity
@Table(name = "job_titles")
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class JobTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_vi", nullable = false)
    private String nameVi;

    private String nameEn;

    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_level_id", nullable = false)
    @JsonIgnoreProperties({ "jobTitles" })
    private PositionLevel positionLevel;

    // ⭐ XÓA List<User> users — quan hệ chuyển sang UserPosition

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        if (!this.active)
            this.active = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}