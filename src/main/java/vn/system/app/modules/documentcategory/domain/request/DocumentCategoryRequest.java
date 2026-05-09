package vn.system.app.modules.documentcategory.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentCategoryRequest {

    @NotBlank(message = "Mã danh mục không được để trống")
    private String categoryCode;

    @NotBlank(message = "Tên danh mục không được để trống")
    private String categoryName;

    private String symbol;

    private String definition;

    private boolean active = true;

    private boolean mappingProcedure = false;
}