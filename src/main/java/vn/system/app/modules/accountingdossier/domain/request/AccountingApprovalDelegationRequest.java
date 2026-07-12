package vn.system.app.modules.accountingdossier.domain.request;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountingApprovalDelegationRequest {

    @NotBlank(message = "Người ủy quyền không được để trống")
    private String delegatorUserId;

    @NotBlank(message = "Người nhận ủy quyền không được để trống")
    private String delegateUserId;

    private Long companyId;

    @NotNull(message = "Ngày bắt đầu hiệu lực không được để trống")
    private Instant validFrom;

    @NotNull(message = "Ngày kết thúc hiệu lực không được để trống")
    private Instant validTo;

    private String scopeType;
    private Long scopeRefId;
    private String reason;
    private boolean activateImmediately;
}
