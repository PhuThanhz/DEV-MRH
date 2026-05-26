package vn.system.app.modules.evaluation.domain.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScoreRequest {

    @NotNull(message = "Tiêu chí không được để trống")
    private Long criteriaId;

    @NotNull(message = "Điểm không được để trống")
    @DecimalMin(value = "1.0", message = "Điểm phải từ 1 đến 5")
    @DecimalMax(value = "5.0", message = "Điểm phải từ 1 đến 5")
    private Double score;
}
