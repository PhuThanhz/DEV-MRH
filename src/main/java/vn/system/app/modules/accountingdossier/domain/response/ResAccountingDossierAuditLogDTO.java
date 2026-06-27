package vn.system.app.modules.accountingdossier.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResAccountingDossierAuditLogDTO {

    private Long id;
    private Long dossierId;
    private String actionType;
    private String note;
    private String actorUserId;
    private String ipAddress;
    private Instant createdAt;
    private String createdBy;
}
