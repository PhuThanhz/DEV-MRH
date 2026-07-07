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
    private String userAgent;
    private String targetType;
    private Long targetId;
    private String fromStatus;
    private String toStatus;
    private String beforeValue;
    private String afterValue;
    private String bulkActionId;
    private Instant createdAt;
    private String createdBy;
}
