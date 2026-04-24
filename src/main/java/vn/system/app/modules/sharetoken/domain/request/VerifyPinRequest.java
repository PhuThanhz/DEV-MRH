package vn.system.app.modules.sharetoken.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyPinRequest {

    @NotBlank
    private String pin;
}