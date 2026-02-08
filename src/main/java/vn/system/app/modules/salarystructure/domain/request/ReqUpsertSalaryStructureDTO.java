package vn.system.app.modules.salarystructure.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.salarystructure.domain.OwnerLevel;

@Getter
@Setter
public class ReqUpsertSalaryStructureDTO {

    @NotNull
    private OwnerLevel ownerLevel;

    @NotNull
    private Long ownerJobTitleId;

    @NotNull
    private Long salaryGradeId;

    /*
     * ============================
     * MONTH KPI COMPONENT
     * ============================
     */
    private Double monthBaseSalary;
    private Double monthPositionAllowance;
    private Double monthMealAllowance;
    private Double monthFuelSupport;
    private Double monthPhoneSupport;
    private Double monthOtherSupport;

    // ⭐ Thưởng hiệu quả công việc A–B–C–D theo tháng
    private Double monthKpiBonusA;
    private Double monthKpiBonusB;
    private Double monthKpiBonusC;
    private Double monthKpiBonusD;

    /*
     * ============================
     * HOUR KPI COMPONENT
     * ============================
     */
    private Double hourBaseSalary;
    private Double hourPositionAllowance;
    private Double hourMealAllowance;
    private Double hourFuelSupport;
    private Double hourPhoneSupport;
    private Double hourOtherSupport;

    // ⭐ Thưởng hiệu quả công việc A–B–C–D theo giờ
    private Double hourKpiBonusA;
    private Double hourKpiBonusB;
    private Double hourKpiBonusC;
    private Double hourKpiBonusD;
}
