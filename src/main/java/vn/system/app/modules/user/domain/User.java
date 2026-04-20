package vn.system.app.modules.user.domain;

import java.time.Instant;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.role.domain.Role;
import vn.system.app.modules.userinfo.domain.UserInfo;

@Entity
@Table(name = "users")
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    private String name;

    @NotBlank(message = "email không được để trống")
    private String email;

    private String password;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String refreshToken;

    @Column(name = "avatar", columnDefinition = "TEXT")
    private String avatar;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "last_login_ip", length = 64)
    private String lastLoginIp;

    @Column(nullable = false)
    private boolean active;

    private String resetCode;
    private Instant resetCodeExpire;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    @JsonIgnoreProperties({ "users", "permissions" })
    private Role role;

    // ⭐ Thêm quan hệ ngược về UserInfo — giúp JOIN FETCH tránh N+1
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({ "user" })
    private UserInfo userInfo;

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