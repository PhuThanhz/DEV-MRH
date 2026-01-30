package vn.system.app.modules.jdflow.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqApproveFlow {

    // Nếu null → kết thúc duyệt
    private Long nextUserId;
}
