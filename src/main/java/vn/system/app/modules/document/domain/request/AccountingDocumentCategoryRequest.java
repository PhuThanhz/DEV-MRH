package vn.system.app.modules.document.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountingDocumentCategoryRequest {

    @NotBlank(message = "Mã loại chứng từ không được để trống")
    @Size(max = 50, message = "Mã loại chứng từ tối đa 50 ký tự")
    private String categoryCode;

    @NotBlank(message = "Tên loại chứng từ không được để trống")
    @Size(max = 200, message = "Tên loại chứng từ tối đa 200 ký tự")
    private String categoryName;

    @Size(max = 20, message = "Ký hiệu tối đa 20 ký tự")
    private String symbol;

    private String description;
    private boolean active = true;
}
