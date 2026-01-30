package vn.system.app.modules.salarygradeperformancerating.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryGradePerformanceRatingRequest {

    private Long salaryGradeId;

    private String ratingAText;
    private String ratingBText;
    private String ratingCText;
    private String ratingDText;

    private Integer status;
}
