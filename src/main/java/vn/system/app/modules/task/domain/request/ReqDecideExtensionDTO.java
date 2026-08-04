package vn.system.app.modules.task.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqDecideExtensionDTO {

    @NotBlank(message = "Quyết định không được để trống")
    private String decision; // "APPROVE" or "REJECT"

    private String decisionNote;
}
