package vn.system.app.modules.task.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqApproveTaskDTO {

    @NotBlank(message = "Quyết định phê duyệt không được để trống")
    private String decision; // "APPROVE" or "REWORK"

    private String reworkReason;
}
