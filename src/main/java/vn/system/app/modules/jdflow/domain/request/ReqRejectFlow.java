package vn.system.app.modules.jdflow.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqRejectFlow {

    @NotBlank(message = "Lý do từ chối không được để trống")
    private String reason;
}