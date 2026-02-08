package vn.system.app.modules.salarystructure.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;

@Entity
@Table(name = "salary_structure")
@Getter
@Setter
public class SalaryStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OwnerLevel ownerLevel;

    @Column(nullable = false)
    private Long ownerJobTitleId;

    @Column(nullable = false)
    private Long salaryGradeId;

    /*
     * ============================
     * MONTH COMPONENT
     * ============================
     */
    private Double monthBaseSalary;
    private Double monthPositionAllowance;
    private Double monthMealAllowance;
    private Double monthFuelSupport;
    private Double monthPhoneSupport;
    private Double monthOtherSupport;

    /* Thưởng hiệu quả công việc (4 mức A–B–C–D) */
    private Double monthKpiBonusA;
    private Double monthKpiBonusB;
    private Double monthKpiBonusC;
    private Double monthKpiBonusD;

    /*
     * ============================
     * HOUR COMPONENT
     * ============================
     */
    private Double hourBaseSalary;
    private Double hourPositionAllowance;
    private Double hourMealAllowance;
    private Double hourFuelSupport;
    private Double hourPhoneSupport;
    private Double hourOtherSupport;

    /* Thưởng hiệu quả công việc theo giờ (A–B–C–D) */
    private Double hourKpiBonusA;
    private Double hourKpiBonusB;
    private Double hourKpiBonusC;
    private Double hourKpiBonusD;

    private Boolean active = true;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        createdAt = Instant.now();
        createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
