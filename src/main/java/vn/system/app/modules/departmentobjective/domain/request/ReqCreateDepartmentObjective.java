package vn.system.app.modules.departmentobjective.domain.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateDepartmentObjective {

    @NotNull(message = "DepartmentId không được để trống")
    private Long departmentId;

    private LocalDate issueDate;

    /*
     * MỤC TIÊU PHÒNG BAN
     */
    private List<ObjectiveItem> objectives;

    /*
     * NHIỆM VỤ THEO BỘ PHẬN
     */
    private List<SectionTask> tasks;

    @Getter
    @Setter
    public static class ObjectiveItem {

        private String content;

        private Integer orderNo;
    }

    @Getter
    @Setter
    public static class SectionTask {

        private Long sectionId;

        private List<TaskItem> items;
    }

    @Getter
    @Setter
    public static class TaskItem {

        private String content;

        private Integer orderNo;
    }
}