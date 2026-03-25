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
     * sectionId có thể null nếu phòng ban không có bộ phận
     */
    private List<SectionTask> tasks;

    /*
     * QUYỀN HẠN
     */
    private List<AuthorityItem> authorities;

    @Getter
    @Setter
    public static class ObjectiveItem {

        private String content;

        private Integer orderNo;
    }

    @Getter
    @Setter
    public static class SectionTask {

        // null = phòng ban không có bộ phận, nhập list thẳng
        private Long sectionId;

        private List<TaskItem> items;
    }

    @Getter
    @Setter
    public static class TaskItem {

        private String content;

        private Integer orderNo;
    }

    @Getter
    @Setter
    public static class AuthorityItem {

        private String content;

        private Integer orderNo;
    }
}