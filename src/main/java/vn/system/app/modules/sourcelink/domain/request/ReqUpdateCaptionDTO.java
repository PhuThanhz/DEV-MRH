package vn.system.app.modules.sourcelink.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateCaptionDTO {

    @NotBlank(message = "Caption không được để trống")
    private String caption;
}
