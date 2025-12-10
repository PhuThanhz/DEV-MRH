package vn.system.app.modules.sourcelink.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateGroupInMainDTO {

    @NotBlank(message = "Tên group không được để trống")
    private String groupName;
}
