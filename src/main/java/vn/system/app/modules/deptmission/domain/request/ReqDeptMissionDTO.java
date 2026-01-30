package vn.system.app.modules.deptmission.domain.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqDeptMissionDTO {

    @NotNull(message = "departmentId không được để trống")
    private Long departmentId;

    @NotBlank(message = "Mục tiêu hoạt động không được để trống")
    private String objectives;

    @NotEmpty(message = "Danh sách nhiệm vụ bộ phận không được để trống")
    private List<SectionTaskDTO> sectionTasks;

    @Getter
    @Setter
    public static class SectionTaskDTO {

        @NotNull(message = "sectionId không được để trống")
        private Long sectionId;

        @NotBlank(message = "Nội dung nhiệm vụ không được để trống")
        private String content;
    }
}
