package vn.system.app.modules.jd.requirement.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;

@Entity
@Table(name = "jd_requirements")
@Getter
@Setter
public class JDRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long jobDescriptionId;

    /*
     * ===============================
     * IV. YÊU CẦU ĐỐI VỚI VỊ TRÍ
     * ===============================
     */

    @Column(columnDefinition = "LONGTEXT")
    private String knowledge; // 1. Kiến thức

    @Column(columnDefinition = "LONGTEXT")
    private String experience; // 2. Kinh nghiệm

    @Column(columnDefinition = "LONGTEXT")
    private String skills; // 3. Kỹ năng

    @Column(columnDefinition = "LONGTEXT")
    private String qualities; // 4. Phẩm chất

    @Column(columnDefinition = "LONGTEXT")
    private String otherRequirements; // 5. Yêu cầu khác

    private Boolean active = true;

    /*
     * ===============================
     * AUDIT
     * ===============================
     */

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }
}