package vn.system.app.modules.companyprocedure.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.companyprocedure.domain.enums.ProcedureStatus;

@Getter
@Setter
public class CompanyProcedureRequest {

    @NotNull(message = "SectionId không được để trống")
    private Long sectionId;

    @NotBlank(message = "Tên quy trình không được để trống")
    private String procedureName;

    private String fileUrl;

    @NotNull(message = "Trạng thái không được để trống")
    private ProcedureStatus status;

    private Integer planYear;

    private String note;

    // ✅ Thêm để đồng bộ với các module khác (Role, JobTitle)
    // Giúp bật/tắt quy trình mà không cần endpoint riêng
    private Boolean active;
}
