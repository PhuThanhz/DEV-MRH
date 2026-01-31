package vn.system.app.modules.user.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.role.domain.Role;
import vn.system.app.modules.jobtitle.domain.JobTitle;

@Entity
@Table(name = "users")
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" }) // FIX lỗi proxy
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @NotBlank(message = "email không được để trống")
    private String email;

    // Cho phép null trong update
    private String password;

    private int age;
    private String address;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String refreshToken;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    /*
     * ==========================
     * ROLE (N-1)
     * ==========================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    @JsonIgnoreProperties({ "users", "permissions" })
    private Role role;

    /*
     * ==========================
     * JOB TITLES (N-N)
     * ==========================
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_job_title", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "job_title_id"))
    @JsonIgnoreProperties({ "users" })
    private List<JobTitle> jobTitles = new ArrayList<>();

    /*
     * ==========================
     * HÀM TÁCH LEVEL TỪ CODE
     * ==========================
     */
    @Transient
    private Integer extractLevel(String code) {
        return Integer.parseInt(code.replaceAll("[^0-9]", ""));
    }

    /*
     * ==========================
     * HÀM TÍNH RANK CẤP BẬC
     *
     * Rank = bandOrder * 1000 + level
     *
     * bandOrder nhỏ hơn → cấp cao hơn
     * level nhỏ hơn → cấp cao hơn trong cùng band
     *
     * rank nhỏ nhất = cấp cao nhất
     * ==========================
     */
    @Transient
    public Integer getRank() {
        if (jobTitles == null || jobTitles.isEmpty()) {
            return null;
        }

        return jobTitles.stream()
                .filter(jt -> jt.getPositionLevel() != null)
                .map(jt -> {
                    int bandOrder = jt.getPositionLevel().getBandOrder();
                    int level = extractLevel(jt.getPositionLevel().getCode());
                    return bandOrder * 1000 + level;
                })
                .min(Integer::compareTo)
                .orElse(null);
    }

    /*
     * ==========================
     * TƯƠNG THÍCH HỆ THỐNG CŨ
     * ==========================
     */
    @Transient
    public Integer getHighestLevel() {
        return getRank();
    }

    /*
     * ==========================
     * AUDIT
     * ==========================
     */
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
