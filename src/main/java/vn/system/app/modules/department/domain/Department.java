package vn.system.app.modules.department.domain;

import java.time.Instant;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.company.domain.Company;

@Entity
@Table(name = "departments")
@Getter
@Setter
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Mã phòng ban không được để trống")
    @Column(unique = true, nullable = false)
    private String code;

    @NotBlank(message = "Tên phòng ban không được để trống")
    @Column(nullable = false)
    private String name;

    private String englishName;

    private Integer status; // 1 = active, 0 = inactive

    // =======================
    // PHÒNG BAN THUỘC CÔNG TY
    // =======================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // =======================
    // ❌ XOÁ — KHÔNG CÒN MANY TO MANY VỚI JOBTITLE
    // =======================
    // private List<JobTitle> jobTitles;

    // =======================
    // AUDIT FIELDS
    // =======================
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        this.status = 1;
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
