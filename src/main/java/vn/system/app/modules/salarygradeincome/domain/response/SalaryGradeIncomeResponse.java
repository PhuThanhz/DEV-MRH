package vn.system.app.modules.salarygradeincome.domain.response;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.salarygradeincome.domain.PayType;

@Getter
@Setter
public class SalaryGradeIncomeResponse {

    private Long id;
    private Long salaryGradeId;
    private Integer gradeLevel;
    private PayType payType;

    private BigDecimal baseSalary;
    private BigDecimal positionAllowance;
    private BigDecimal mealAllowance;
    private BigDecimal fuelAllowance;
    private BigDecimal phoneAllowance;
    private BigDecimal otherAllowance;

    private BigDecimal bonusA;
    private BigDecimal bonusB;
    private BigDecimal bonusC;
    private BigDecimal bonusD;

    private Integer status;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
