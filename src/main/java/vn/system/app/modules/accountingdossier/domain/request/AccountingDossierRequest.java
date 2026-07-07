package vn.system.app.modules.accountingdossier.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierCategoryMode;

@Getter
@Setter
public class AccountingDossierRequest {

    @NotBlank(message = "Nội dung bộ chứng từ không được để trống")
    @Size(max = 1000, message = "Nội dung bộ chứng từ tối đa 1000 ký tự")
    private String content;

    private AccountingDossierCategoryMode categoryMode;

    private Long dossierCategoryId;

    @Size(max = 255, message = "Tên danh mục phi cấu trúc tối đa 255 ký tự")
    private String customCategoryName;

    private Boolean syncCategoryRequested;

    @NotNull(message = "Công ty không được để trống")
    private Long companyId;

    @NotNull(message = "Phòng ban không được để trống")
    private Long departmentId;

    private Long sectionId;
}
