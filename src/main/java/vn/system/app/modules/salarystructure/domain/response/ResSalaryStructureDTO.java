package vn.system.app.modules.salarystructure.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.salarystructure.domain.OwnerLevel;

@Getter
@Setter
public class ResSalaryStructureDTO {

    private Long id;
    private OwnerLevel ownerLevel;
    private Long ownerJobTitleId;
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

    // ⭐ Thưởng hiệu quả công việc theo tháng A–B–C–D
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

    // ⭐ Thưởng hiệu quả công việc theo giờ A–B–C–D
    private Double hourKpiBonusA;
    private Double hourKpiBonusB;
    private Double hourKpiBonusC;
    private Double hourKpiBonusD;

    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
