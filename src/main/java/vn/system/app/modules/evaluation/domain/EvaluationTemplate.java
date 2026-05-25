package vn.system.app.modules.evaluation.domain;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.evaluation.domain.enums.TemplateStatus;
import vn.system.app.modules.evaluation.domain.enums.TemplateType;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.company.domain.Company;
import vn.system.app.modules.jobtitle.domain.JobTitle;


/**
 * Mẫu form đánh giá HQCV.
 * Một template có nhiều section (phần A, B, C...).
 * Sau khi template đã được dùng trong kỳ ACTIVE thì không cho sửa nữa.
 */
@Entity
@Table(name = "evaluation_templates")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EvaluationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    /**
     * STAFF: mẫu F02 dành cho nhân viên thường.
     * MANAGER: mẫu F01 dành cho cấp quản lý.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TemplateType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TemplateStatus status = TemplateStatus.DRAFT;

    // Các section (Phần A, B, C...) thuộc template này
    @OneToMany(mappedBy = "template", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties("template")
    private List<TemplateSection> sections;

    // Danh sách chức danh áp dụng cụ thể (Tùy chọn, để trống = áp dụng cho tất cả)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "evaluation_template_target_job_titles",
        joinColumns = @JoinColumn(name = "template_id"),
        inverseJoinColumns = @JoinColumn(name = "job_title_id")
    )
    @JsonIgnoreProperties({"company", "positionLevel"})
    private List<JobTitle> targetJobTitles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    @JsonIgnoreProperties({ "subordinates", "directManager", "userInfo", "role" })
    private User createdByUser;

    @NotNull(message = "Công ty không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false) // Bắt buộc phải thuộc một công ty cụ thể
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Company company;



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
