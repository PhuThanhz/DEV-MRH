package vn.system.app.modules.departmentprocedure.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateDepartmentProcedure {

    private Long id;

    private String procedureName;

    private String status;

    private Integer planYear;

    private String fileUrl;

    private String note;

    private boolean active;
}