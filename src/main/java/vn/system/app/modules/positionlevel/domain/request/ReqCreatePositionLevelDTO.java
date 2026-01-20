package vn.system.app.modules.positionlevel.domain.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreatePositionLevelDTO {

    @NotBlank(message = "Code không được để trống")
    private String code;

    // nullable – chỉ bắt buộc khi tạo cấp đầu tiên (S1, M1...)
    private Integer bandOrder;
}
