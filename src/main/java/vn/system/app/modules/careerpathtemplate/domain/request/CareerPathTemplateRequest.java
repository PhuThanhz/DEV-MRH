package vn.system.app.modules.careerpathtemplate.domain.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CareerPathTemplateRequest {

    @NotBlank(message = "Tên lộ trình không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Phòng ban không được để trống")
    private Long departmentId; // ← thêm — template thuộc phòng ban nào

    @NotEmpty(message = "Lộ trình phải có ít nhất 1 bước")
    @Valid
    private List<StepRequest> steps;

    // ── Inner DTO ──────────────────────────────────────
    @Getter
    @Setter
    public static class StepRequest {

        @NotNull(message = "Thứ tự bước không được để trống")
        private Integer stepOrder;

        @NotNull(message = "Chức danh không được để trống")
        private Long careerPathId;

        private Integer durationMonths; // null = bước cuối (đỉnh)

        private String description;
    }
}