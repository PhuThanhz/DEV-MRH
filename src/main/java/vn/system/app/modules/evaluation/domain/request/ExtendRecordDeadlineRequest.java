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

    @jakarta.validation.Valid
    private List<RecordDeadlineOverride> recordDeadlines;

    @jakarta.validation.Valid
    private List<PhaseDeadlineOverride> phaseDeadlines;

    private String reason;

    private boolean cascade = true;

    public enum Phase {
        EMPLOYEE,
        MANAGER,
        APPROVAL
    }

    @Getter
    @Setter
    public static class RecordDeadlineOverride {
        @NotNull(message = "recordId không được để trống")
        private Long recordId;

        @NotNull(message = "deadline không được để trống")
        private Instant deadline;
    }

    @Getter
    @Setter
    public static class PhaseDeadlineOverride {
        @NotNull(message = "phase không được để trống")
        private Phase phase;

        @NotNull(message = "deadline không được để trống")
        private Instant deadline;
    }
}
