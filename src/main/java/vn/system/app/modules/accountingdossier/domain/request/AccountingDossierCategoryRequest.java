package vn.system.app.modules.accountingdossier.domain.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AccountingDossierCategoryRequest {

    @Size(max = 100, message = "Mã mẫu bộ chứng từ không được vượt quá 100 ký tự")
    private String categoryCode;

    @NotBlank(message = "Tên mẫu không được để trống")
    private String categoryName;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    private Long companyId;

    @Pattern(regexp = "^(COMPANY|GLOBAL)?$", message = "Phạm vi (scope) chỉ nhận giá trị COMPANY hoặc GLOBAL")
    private String scope; // COMPANY, GLOBAL

    private boolean active = true;

    private List<Long> documentCategoryIds;

    @Valid
    private List<DocumentCategoryItemRequest> documentCategoryItems;

    @Getter
    @Setter
    public static class DocumentCategoryItemRequest {
        @NotNull(message = "ID loại chứng từ không được để trống")
        private Long documentCategoryId;
        private Boolean required;
        private Integer sortOrder;
    }
}
