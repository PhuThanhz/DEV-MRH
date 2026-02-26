package vn.system.app.modules.jd.requirement.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResJDRequirementDTO {

    private Long id;
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

    /*
     * ===============================
     * AUDIT
     * ===============================
     */

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}