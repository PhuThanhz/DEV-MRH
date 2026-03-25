package vn.system.app.modules.positionlevel.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreatePositionLevelDTO {

    @NotBlank(message = "Code không được để trống")
    private String code;

    // nullable – chỉ bắt buộc khi tạo cấp đầu tiên (S1, M1...)
    private Integer bandOrder;

    // ⭐ THÊM MỚI — bắt buộc phải biết tạo cho công ty nào
    @NotNull(message = "CompanyId không được để trống")
    private Long companyId;
}