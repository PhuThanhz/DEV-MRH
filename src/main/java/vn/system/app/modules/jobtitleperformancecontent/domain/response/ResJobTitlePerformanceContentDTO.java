package vn.system.app.modules.jobtitleperformancecontent.domain.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.jobtitleperformancecontent.domain.OwnerLevel;

@Getter
@Setter
public class ResJobTitlePerformanceContentDTO {

    private Long id;
    private OwnerLevel ownerLevel;
    private Long ownerJobTitleId;

    private Long salaryGradeId;
    private Integer salaryGradeNumber;

    private String contentA;
    private String contentB;
    private String contentC;
    private String contentD;

    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
