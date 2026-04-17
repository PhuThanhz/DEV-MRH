package vn.system.app.modules.jd.jdflow.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqSubmitJdFlow {

    @NotNull(message = "jdId không được để trống")
    private Long jdId;

    // Bỏ @NotNull — backend tự xử lý null khi RETURNED
    private Long nextUserId;

}