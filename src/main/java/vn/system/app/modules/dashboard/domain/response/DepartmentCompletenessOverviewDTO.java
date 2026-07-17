package vn.system.app.modules.dashboard.domain.response;

import java.util.List;

public record DepartmentCompletenessOverviewDTO(
        long total,
        long full,
        long partial,
        long empty,
        long missingOrgChart,
        long missingObjectives,
        long missingDepartmentProcedure,
        long missingPermissions,
        long missingCareerPath,
        long missingSalaryGrade,
        long missingJobTitleMap,
        List<DepartmentCompletenessDTO> topMissing) {
}
