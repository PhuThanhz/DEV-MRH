package vn.system.app.modules.dashboard.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentCompletenessDTO {

    private Long departmentId;
    private String departmentName;
    private String companyName; // ← THÊM

    private boolean orgChart;
    private boolean objectives;
    private boolean departmentProcedure;
    private boolean permissions;
    private boolean careerPath;
    private boolean salaryGrade;
    private boolean jobTitleMap;

    private int score;
}