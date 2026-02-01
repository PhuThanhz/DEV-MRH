package vn.system.app.modules.jobtitle.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateJobTitleDTO {

    @NotBlank(message = "Tên chức danh không được để trống")
    private String nameVi;

    private String nameEn;

    @NotNull(message = "PositionLevelId không được để trống")
    private Long positionLevelId;

    private Boolean active = true; // thêm để đồng bộ
}
