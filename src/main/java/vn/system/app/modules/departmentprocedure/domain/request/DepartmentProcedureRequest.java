package vn.system.app.modules.departmentprocedure.domain.request;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentProcedureRequest {

    @NotBlank(message = "Mã quy trình không được để trống")
    private String procedureCode;

    @NotBlank(message = "Tên quy trình không được để trống")
    private String procedureName;

    private String status;
    private Integer planYear;
    private Instant issuedDate; // ← THÊM

    private List<String> fileUrls;

    private String note;
    private boolean active;

    @NotNull(message = "Phòng ban không được để trống")
    private Long departmentId;

    private Long sectionId;
}