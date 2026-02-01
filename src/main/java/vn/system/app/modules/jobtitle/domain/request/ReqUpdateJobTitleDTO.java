package vn.system.app.modules.jobtitle.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateJobTitleDTO {

    @NotNull(message = "Id không được để trống")
    private Long id;

    private String nameVi;
    private String nameEn;

    private Boolean active; // đổi từ status → active

    private Long positionLevelId;
}
