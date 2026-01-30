package vn.system.app.modules.jdflow.domain.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResJobDescriptionFlowDTO {

    private Long id;

    // JD đang được duyệt
    private Long jobDescriptionId;

    // Người gửi duyệt (người tạo JD)
    private Long fromUserId;

    // Người đang được giao duyệt (hoặc null nếu chờ ban hành)
    private Long toUserId;

    // Trạng thái flow
    private String status;

    private Instant createdAt;
    private Instant updatedAt;
}
