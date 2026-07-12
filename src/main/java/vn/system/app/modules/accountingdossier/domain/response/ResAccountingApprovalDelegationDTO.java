package vn.system.app.modules.accountingdossier.domain.response;

import java.time.Instant;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.accountingdossier.domain.enums.DelegationStatus;

@Getter
@Setter
@Builder
public class ResAccountingApprovalDelegationDTO {
    private Long id;
    private String delegatorUserId;
    private String delegatorName;
    private String delegatorEmail;
    private String delegateUserId;
    private String delegateName;
    private String delegateEmail;
    private Long companyId;
    private Instant validFrom;
    private Instant validTo;
    private String scopeType;
    private Long scopeRefId;
    private String reason;
    private DelegationStatus status;
    private Instant createdAt;
    private Instant revokedAt;
}
