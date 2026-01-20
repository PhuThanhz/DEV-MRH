package vn.system.app.modules.department.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDepartmentRequest {

    @NotBlank(message = "Mã phòng ban không được để trống")
    private String code; // mã phòng ban

    @NotBlank(message = "Tên phòng ban không được để trống")
    private String name; // tên phòng ban

    private String englishName; // tên tiếng Anh (không bắt buộc)

    @NotNull(message = "CompanyId không được để trống")
    private Long companyId; // id công ty
}
