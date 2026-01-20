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
    private Long id; // id phòng ban

    @NotBlank(message = "Mã phòng ban không được để trống")
    @Column(unique = true)
    private String code; // mã phòng ban

    @NotBlank(message = "Tên phòng ban không được để trống")
    private String name; // tên phòng ban

    private String englishName; // tên tiếng Anh (không bắt buộc)

    private Integer status; // 1 = active, 0 = inactive

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company; // thuộc công ty nào

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.status = 1;

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
