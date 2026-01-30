package vn.system.app.modules.jdflow.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateFlow {

    private Long jobDescriptionId; // JD cần gửi duyệt
    private Long toUserId; // Người duyệt đầu tiên
}
