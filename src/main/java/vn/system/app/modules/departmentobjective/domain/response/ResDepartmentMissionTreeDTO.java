package vn.system.app.modules.departmentobjective.domain.response;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResDepartmentMissionTreeDTO {

    private DepartmentInfo department;

    private LocalDate issueDate;

    private List<ObjectiveItem> objectives;

    private List<SectionTask> tasks;

    @Getter
    @Setter
    public static class DepartmentInfo {

        private Long id;

        private String name;
    }

    @Getter
    @Setter
    public static class ObjectiveItem {

        private Long id;

        private String content;
    }

    @Getter
    @Setter
    public static class SectionTask {

        private Long sectionId;

        private String sectionName;

        private List<TaskItem> tasks;
    }

    @Getter
    @Setter
    public static class TaskItem {

        private Long id;

        private String content;
    }
}