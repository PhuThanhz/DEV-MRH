package vn.system.app.modules.evaluation.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddPeriodEmployeeRequest {
    @NotBlank(message = "Nhân viên không được để trống")
    private String employeeId;

    @NotBlank(message = "Quản lý trực tiếp không được để trống")
    private String directManagerId;

    @NotNull(message = "Biểu mẫu đánh giá không được để trống")
    private Long templateId;
}
