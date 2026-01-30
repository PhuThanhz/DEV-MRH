package vn.system.app.modules.user.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @NotBlank(message = "password không được để trống")
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
    @JsonIgnoreProperties({ "users", "permissions" }) // tránh vòng lặp JSON
    private Role role;

    /*
     * ==========================
     * JOB TITLES (N-N)
     * ==========================
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_job_title", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "job_title_id"))
    @JsonIgnoreProperties({ "users" }) // tránh load ngược
    private List<JobTitle> jobTitles = new ArrayList<>();

    /*
     * ==========================
     * HÀM LẤY CẤP CAO NHẤT
     * ==========================
     */
    @Transient
    public Integer getHighestLevel() {
        if (jobTitles == null || jobTitles.isEmpty())
            return null;

        return jobTitles.stream()
                .filter(jt -> jt.getPositionLevel() != null)
                .map(jt -> jt.getPositionLevel().getBandOrder())
                .min(Integer::compareTo)
                .orElse(null);
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
