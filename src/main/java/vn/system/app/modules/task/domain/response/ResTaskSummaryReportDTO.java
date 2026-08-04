package vn.system.app.modules.task.domain.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResTaskSummaryReportDTO {

    private Integer totalTaskCount = 0;
    private Integer completedTaskCount = 0;
    private Integer onTimeTaskCount = 0;
    private Double onTimePercentage = 0.0;

    private List<DepartmentGroupDTO> departmentGroups;

    @Getter
    @Setter
    public static class DepartmentGroupDTO {
        private Long departmentId;
        private String departmentName;
        private String companyName;
        private Integer taskCount = 0;
        private Integer onTimeCount = 0;
        private Double onTimePercentage = 0.0;
        private List<EmployeeGroupDTO> employeeGroups;
    }

    @Getter
    @Setter
    public static class EmployeeGroupDTO {
        private String assigneeId;
        private String assigneeName;
        private String assigneeAvatar;
        private Integer taskCount = 0;
        private Integer onTimeCount = 0;
        private Double onTimePercentage = 0.0;
        private List<ResTaskDTO> tasks;
    }
}
