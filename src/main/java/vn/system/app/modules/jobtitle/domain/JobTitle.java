package vn.system.app.modules.jobtitle.domain;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.positionlevel.domain.PositionLevel;
import vn.system.app.modules.user.domain.User;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "job_titles")
@Getter
@Setter
public class JobTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * =========================
     * BASIC INFO
     * =========================
     */
    @Column(name = "name_vi", nullable = false)
    private String nameVi;

    @Column(name = "name_en")
    private String nameEn;

    @Column(nullable = false)
    private Integer status;

    /*
     * =========================
     * RELATION — POSITION LEVEL (N:1)
     * =========================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_level_id", nullable = false)
    @JsonIgnoreProperties({ "jobTitles" }) // tránh vòng lặp
    private PositionLevel positionLevel;

    /*
     * =========================
     * RELATION — USERS (N:N)
     * =========================
     */
    @ManyToMany(mappedBy = "jobTitles", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({ "jobTitles", "role" }) // tránh vòng lặp & lazy proxy
    private List<User> users;

    /*
     * =========================
     * AUDIT
     * =========================
     */
    @Column(updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    /*
     * =========================
     * LIFECYCLE
     * =========================
     */
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.status == null) {
            this.status = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
