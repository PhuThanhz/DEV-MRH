// vn.system.app.modules.careerpath.domain.response.CareerPathPreviewResponse

package vn.system.app.modules.careerpath.domain.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * Dùng cho endpoint /preview — trả về danh sách sẽ được tạo
 * mà KHÔNG ghi vào DB.
 */
@Getter
@Builder
public class CareerPathPreviewResponse {

    /** Các jobTitle sẽ được tạo mới */
    private List<PreviewItem> willCreate;

    /** Các jobTitle đã tồn tại, sẽ bị skip */
    private List<PreviewItem> willSkip;

    @Getter
    @Builder
    public static class PreviewItem {
        private Long jobTitleId;
        private String jobTitleName;
        private String positionLevelCode;
        private String reason; // chỉ có giá trị ở willSkip
    }
}