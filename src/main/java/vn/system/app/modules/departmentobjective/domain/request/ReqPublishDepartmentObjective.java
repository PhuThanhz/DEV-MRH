package vn.system.app.modules.departmentobjective.domain.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqPublishDepartmentObjective {

    @NotNull(message = "DepartmentId không được để trống")
    private Long departmentId;

    private String title;

    private String changeSummary;

    private LocalDate effectiveDate;
}
