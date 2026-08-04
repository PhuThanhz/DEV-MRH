package vn.system.app.modules.approvaldelegation.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

import vn.system.app.modules.approvaldelegation.domain.enums.ApprovalDelegationStatus;

@Getter
@Setter
public class ResApprovalDelegationDTO {

    private Long id;
    private String module;

    private String delegatorUserId;
    private String delegatorUserName;
    private String delegatorUserAvatar;

    private String delegateUserId;
    private String delegateUserName;
    private String delegateUserAvatar;

    private Instant validFrom;
    private Instant validTo;
    private ApprovalDelegationStatus status;
    private String reason;
    private String scopeType;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
