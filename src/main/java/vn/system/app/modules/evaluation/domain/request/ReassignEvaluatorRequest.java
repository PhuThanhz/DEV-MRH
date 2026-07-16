package vn.system.app.modules.evaluation.domain.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReassignEvaluatorRequest {

    @NotEmpty(message = "Danh sách bản đánh giá không được để trống")
    private List<Long> recordIds;

    @NotNull(message = "Vai trò người chấm không được để trống")
    private EvaluatorRole evaluatorRole;

    @NotBlank(message = "Người chấm mới không được để trống")
    private String newEvaluatorUserId;

    private String reason;

    public enum EvaluatorRole {
        DIRECT_MANAGER,
        INDIRECT_MANAGER
    }
}
