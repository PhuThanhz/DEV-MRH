package vn.system.app.modules.departmentprocedure.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResDepartmentProcedureDTO {

    private Long id;

    private String procedureName;

    private String companyName;

    private String departmentName;

    private String sectionName;

    private String status;

    private Integer planYear;

    private String fileUrl;

    private String note;

    private boolean active;

    private Instant createdAt;
}