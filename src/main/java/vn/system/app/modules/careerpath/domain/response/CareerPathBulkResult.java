// vn.system.app.modules.careerpath.domain.response.CareerPathBulkResult

package vn.system.app.modules.careerpath.domain.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * Kết quả trả về sau khi bulk-create.
 *
 * created → list các record đã được tạo thành công
 * skipped → list jobTitleId đã tồn tại trong department, bị bỏ qua
 */
@Getter
@Builder
public class CareerPathBulkResult {

    private List<CareerPathResponse> created;

    /**
     * Map jobTitleId → lý do bị skip.
     * Dùng List<SkippedItem> để dễ serialize JSON hơn Map.
     */
    private List<SkippedItem> skipped;

    private int totalRequested;
    private int totalCreated;
    private int totalSkipped;

    @Getter
    @Builder
    public static class SkippedItem {
        private Long jobTitleId;
        private String jobTitleName; // để frontend hiển thị cho rõ
        private String reason; // e.g. "Đã tồn tại trong phòng ban này"
    }
}