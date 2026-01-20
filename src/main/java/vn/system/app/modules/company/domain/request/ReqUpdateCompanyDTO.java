package vn.system.app.modules.company.domain.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateCompanyDTO {

    @NotNull(message = "Id công ty không được để trống")
    private Long id;

    @NotBlank(message = "Tên công ty không được để trống")
    private String name; // tên công ty (tiếng Việt)

    private String englishName; // tên công ty tiếng Anh
}
