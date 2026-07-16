package vn.system.app.modules.accountingdossier.domain.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountingDossierActionRequest {

    @Size(max = 1000, message = "Lý do/ghi chú tối đa 1000 ký tự")
    private String note;

    @Size(max = 36, message = "Người nhận kế tiếp không hợp lệ")
    private String nextApproverUserId;
}
