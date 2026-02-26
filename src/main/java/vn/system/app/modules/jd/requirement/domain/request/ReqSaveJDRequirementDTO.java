package vn.system.app.modules.jd.requirement.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqSaveJDRequirementDTO {

    @NotNull
    private Long jobDescriptionId;

    /*
     * ===============================
     * IV. YÊU CẦU ĐỐI VỚI VỊ TRÍ
     * ===============================
     */

    private String knowledge; // 1. Kiến thức
    private String experience; // 2. Kinh nghiệm
    private String skills; // 3. Kỹ năng
    private String qualities; // 4. Phẩm chất
    private String otherRequirements; // 5. Yêu cầu khác
}