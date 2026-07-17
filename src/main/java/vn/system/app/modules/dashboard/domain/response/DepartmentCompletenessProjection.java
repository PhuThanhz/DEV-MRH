package vn.system.app.modules.dashboard.domain.response;

public interface DepartmentCompletenessProjection {
    Long getDepartmentId();

    String getDepartmentName();

    String getCompanyName();

    Integer getOrgChart();

    Integer getObjectives();

    Integer getDepartmentProcedure();

    Integer getPermissions();

    Integer getCareerPath();

    Integer getSalaryGrade();

    Integer getJobTitleMap();
}
