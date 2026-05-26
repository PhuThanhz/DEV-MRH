package vn.system.app.modules.evaluation.domain.request;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtendRecordDeadlineRequest {

    @NotEmpty(message = "Danh sách bản đánh giá không được để trống")
    private List<Long> recordIds;

    @NotNull(message = "Giai đoạn gia hạn không được để trống")
    private Phase phase;

    @NotNull(message = "Hạn mới không được để trống")
    private Instant deadline;

    private String reason;

    private boolean cascade = true;

    public enum Phase {
        EMPLOYEE,
        MANAGER,
        APPROVAL
    }
}
