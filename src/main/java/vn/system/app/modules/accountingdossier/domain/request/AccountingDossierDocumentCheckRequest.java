package vn.system.app.modules.accountingdossier.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountingDossierDocumentCheckRequest {

    @NotBlank(message = "Trạng thái kiểm tra không được để trống")
    private String checkStatus;

    @Size(max = 1000, message = "Ghi chú kiểm tra tối đa 1000 ký tự")
    private String note;
}
