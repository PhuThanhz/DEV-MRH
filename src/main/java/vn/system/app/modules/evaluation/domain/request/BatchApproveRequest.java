package vn.system.app.modules.evaluation.domain.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchApproveRequest {

    @NotEmpty(message = "Danh sách bản đánh giá không được để trống")
    private List<Long> recordIds;
}
