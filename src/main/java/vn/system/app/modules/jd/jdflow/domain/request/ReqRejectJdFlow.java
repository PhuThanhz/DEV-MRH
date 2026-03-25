package vn.system.app.modules.jd.jdflow.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqRejectJdFlow {

    @NotNull(message = "jdId không được để trống")
    private Long jdId;

    @NotBlank(message = "Nội dung từ chối không được để trống")
    private String comment;

}