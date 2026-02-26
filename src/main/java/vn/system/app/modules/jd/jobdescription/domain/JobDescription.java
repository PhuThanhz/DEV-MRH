package vn.system.app.modules.jd.jobdescription.domain;

import java.time.Instant;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;

@Entity
@Table(name = "job_descriptions")
@Getter
@Setter
public class JobDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= HEADER =================

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String companyName;

    private String issueNumber;
    private Instant issueDate;
    private String pageTotal;

    // ⭐ NEW FIELD — Mã số JD
    private String code;

    // ⭐ NEW FIELD — Lần ban hành
    private String revision;

    // ================= POSITION INFO =================

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String jobTitleName;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String departmentName;

    private String belongsTo;
    private String directManager;
    private String workWith;

    // ================= ASSIGNMENT =================

    private String assignerTitle;
    private String assignerName;

    // ================= STATUS =================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JDStatus status = JDStatus.DRAFT;

    // ================= AUDIT =================

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