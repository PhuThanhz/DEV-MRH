package vn.system.app.modules.deptmission.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResDeptMissionDTO {

    private DepartmentDTO department;
    private String objectives;
    private List<SectionTaskDTO> sectionTasks;
    private Instant createdAt;
    private Instant updatedAt;

    @Getter
    @Setter
    public static class DepartmentDTO {
        private Long id;
        private String code;
        private String name;
    }

    @Getter
    @Setter
    public static class SectionTaskDTO {
        private Long sectionId;
        private String sectionName;
        private String content;
    }
}
