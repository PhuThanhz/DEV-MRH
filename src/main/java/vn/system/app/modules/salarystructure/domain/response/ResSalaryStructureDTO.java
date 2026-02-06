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

    /* MONTH */
    private Double monthBaseSalary;
    private Double monthPositionAllowance;
    private Double monthMealAllowance;
    private Double monthFuelSupport;
    private Double monthPhoneSupport;
    private Double monthOtherSupport;
    private Double monthKpiBonus;

    /* HOUR */
    private Double hourBaseSalary;
    private Double hourPositionAllowance;
    private Double hourMealAllowance;
    private Double hourFuelSupport;
    private Double hourPhoneSupport;
    private Double hourOtherSupport;
    private Double hourKpiBonus;

    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
