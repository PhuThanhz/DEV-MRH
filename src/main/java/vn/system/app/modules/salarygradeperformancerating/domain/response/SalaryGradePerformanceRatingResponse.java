package vn.system.app.modules.salarygradeperformancerating.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryGradePerformanceRatingResponse {

    private long id;

    private Long salaryGradeId;
    private Integer gradeLevel;

    private String ratingAText;
    private String ratingBText;
    private String ratingCText;
    private String ratingDText;

    private Integer status;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
