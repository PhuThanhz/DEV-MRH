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

    /*
     * FE dùng flag này để biết render list thẳng hay nhóm theo bộ phận
     */
    private boolean hasSections;

    /*
     * MỤC TIÊU
     */
    private List<ObjectiveItem> objectives;

    /*
     * NHIỆM VỤ THEO BỘ PHẬN (hasSections = true)
     */
    private List<SectionTask> tasks;

    /*
     * NHIỆM VỤ KHÔNG CÓ BỘ PHẬN (hasSections = false)
     */
    private List<TaskItem> generalTasks;

    /*
     * QUYỀN HẠN — null/empty thì FE không hiển thị
     */
    private List<AuthorityItem> authorities;

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

    @Getter
    @Setter
    public static class AuthorityItem {

        private Long id;

        private String content;
    }
}