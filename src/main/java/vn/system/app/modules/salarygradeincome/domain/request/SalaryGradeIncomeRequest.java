package vn.system.app.modules.salarygradeincome.domain.request;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.salarygradeincome.domain.PayType;

@Getter
@Setter
public class SalaryGradeIncomeRequest {

    private Long salaryGradeId;
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
}
