package vn.system.app.modules.documentcategory.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentCategoryRequest {

    @NotBlank(message = "Mã danh mục không được để trống")
    @Size(max = 50, message = "Mã danh mục tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_]*$", message = "Mã danh mục chỉ gồm chữ, số và gạch dưới")
    private String categoryCode;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 200, message = "Tên danh mục tối đa 200 ký tự")
    private String categoryName;

    @Size(max = 20, message = "Ký hiệu tối đa 20 ký tự")
    private String symbol;

    private String definition;

    private boolean active = true;

    private boolean mappingProcedure = false;
}