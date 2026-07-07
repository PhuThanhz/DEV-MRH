package vn.system.app.modules.accountingdossier.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AccountingDossierCategoryRequest {

    private String categoryCode;

    @NotBlank(message = "Tên mẫu không được để trống")
    private String categoryName;

    private String description;

    private Long companyId;

    private String scope; // COMPANY, GLOBAL

    private boolean active = true;

    private List<Long> documentCategoryIds;

    private List<DocumentCategoryItemRequest> documentCategoryItems;

    @Getter
    @Setter
    public static class DocumentCategoryItemRequest {
        private Long documentCategoryId;
        private Boolean required;
        private Integer sortOrder;
    }
}
