package vn.system.app.modules.userinfo.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.user.domain.User;

@Entity
@Table(name = "user_info")
@Getter
@Setter
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "employee_code", length = 50, unique = true)
    private String employeeCode;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "date_of_birth")
    private Instant dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "contract_sign_date")
    private Instant contractSignDate;

    @Column(name = "contract_expire_date")
    private Instant contractExpireDate;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    public enum Gender {
        MALE, FEMALE, OTHER
    }

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