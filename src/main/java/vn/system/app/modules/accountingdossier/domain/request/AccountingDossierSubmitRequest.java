package vn.system.app.modules.accountingdossier.domain.request;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountingDossierSubmitRequest {
    
    @Valid
    @Size(max = 20, message = "Số lượng bước quy trình tùy chỉnh tối đa là 20 bước")
    private List<CustomStep> customSteps;

    @Getter
    @Setter
    public static class CustomStep {
        @NotBlank(message = "Mã bước quy trình không được để trống")
        @Size(max = 100, message = "Mã bước quy trình không được vượt quá 100 ký tự")
        private String stepKey;

        @Size(max = 200, message = "Tên bước quy trình không được vượt quá 200 ký tự")
        private String stepName;

        @Min(value = 1, message = "Thứ tự bước phải lớn hơn hoặc bằng 1")
        @Max(value = 50, message = "Thứ tự bước tối đa là 50")
        private int stepOrder;

        @NotBlank(message = "Loại người duyệt không được để trống")
        private String approverType; // DEPARTMENT_MANAGER, ACCOUNTANT, CHIEF_ACCOUNTANT, CUSTOM_USER

        @Size(max = 50, message = "ID người duyệt tối đa 50 ký tự")
        private String approverUserId;
    }
}
