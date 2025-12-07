package vn.system.app.modules.sourcelink.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateGroupDTO {

    @NotBlank(message = "Tên group không được để trống")
    private String groupName;

    @NotEmpty(message = "Danh sách URL không được để trống")
    private List<String> urls;
}
