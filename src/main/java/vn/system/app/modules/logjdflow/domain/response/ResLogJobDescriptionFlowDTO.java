package vn.system.app.modules.logjdflow.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResLogJobDescriptionFlowDTO {

    private Long id;

    private String action; // SEND, APPROVE, REJECT, ISSUE

    private Long fromUserId;
    private Long toUserId;

    private String comment;

    private Instant createdAt;
    private String createdBy;
}
