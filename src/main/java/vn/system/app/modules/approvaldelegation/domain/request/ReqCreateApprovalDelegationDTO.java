package vn.system.app.modules.approvaldelegation.domain.request;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateApprovalDelegationDTO {

    @NotBlank(message = "Tên module ủy quyền không được để trống")
    private String module = "TASK";

    @NotBlank(message = "Người được ủy quyền không được để trống")
    private String delegateUserId;

    @NotNull(message = "Thời điểm bắt đầu ủy quyền không được để trống")
    private Instant validFrom;

    @NotNull(message = "Thời điểm kết thúc ủy quyền không được để trống")
    private Instant validTo;

    private String reason;
}
