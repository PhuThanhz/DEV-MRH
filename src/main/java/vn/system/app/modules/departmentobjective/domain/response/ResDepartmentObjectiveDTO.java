package vn.system.app.modules.departmentobjective.domain.response;

import java.time.Instant;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResDepartmentObjectiveDTO {

    private Long id;

    private String type;

    private String content;

    private Integer orderNo;

    private LocalDate issueDate;

    private Integer status;

    private Instant createdAt;

    private Instant updatedAt;

    private DepartmentInfo department;

    private CompanyInfo company;

    private SectionInfo section;

    @Getter
    @Setter
    public static class DepartmentInfo {
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    public static class CompanyInfo {
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    public static class SectionInfo {
        private Long id;
        private String name;
    }
}