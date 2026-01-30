package vn.system.app.modules.processaction.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateProcessActionDTO {

    @NotBlank(message = "code không được để trống")
    private String code;

    @NotBlank(message = "name không được để trống")
    private String name;

    private String shortDescription;
    private String description;
    private boolean isActive = true;
}
