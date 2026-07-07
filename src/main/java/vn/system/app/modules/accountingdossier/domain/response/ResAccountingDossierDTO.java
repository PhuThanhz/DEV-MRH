package vn.system.app.modules.accountingdossier.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierCategoryMode;
import vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierStatus;
import vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierStorageStatus;

@Getter
@Setter
public class ResAccountingDossierDTO {

    private Long id;
    private String dossierCode;
    private String content;
    private AccountingDossierCategoryMode categoryMode;
    private String customCategoryName;
    private Ref dossierCategory;
    private Integer dossierCategoryVersion;
    private boolean syncCategoryRequested;

    private Ref company;
    private Ref department;
    private Ref section;

    private String creatorId;
    private AccountingDossierStatus status;
    private AccountingDossierStorageStatus storageStatus;
    private Integer retentionYears;
    private Instant retentionUntil;
    private Instant submittedAt;
    private Instant approvedAt;
    private Instant terminatedAt;
    private Integer returnCount;
    private boolean active;

    private String qrToken;
    private String qrCode;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @Getter
    @Setter
    public static class Ref {
        private Long id;
        private String code;
        private String name;
    }
}
