package vn.system.app.modules.evaluation.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.evaluation.domain.enums.TrainingGroup;

@Getter
@Setter
public class TrainingPlanRequest {

    @NotNull(message = "Nhóm đào tạo không được để trống")
    private TrainingGroup trainingGroup;

    @NotBlank(message = "Nội dung đào tạo không được để trống")
    private String content;

    private String requirements;

    private String solution;

    private String completionTimeline;
}
