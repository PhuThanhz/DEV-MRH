package vn.system.app.modules.task.domain.request;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqRequestExtensionDTO {

    @NotNull(message = "Hạn chót đề xuất không được để trống")
    private Instant requestedDueDate;

    @NotBlank(message = "Lý do xin gia hạn không được để trống")
    private String reason;
}
