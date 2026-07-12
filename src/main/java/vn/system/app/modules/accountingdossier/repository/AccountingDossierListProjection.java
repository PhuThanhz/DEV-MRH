package vn.system.app.modules.accountingdossier.repository;

import java.time.Instant;
import vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierCategoryMode;
import vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierStatus;
import vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierStorageStatus;

public interface AccountingDossierListProjection {
    Long getId();
    String getDossierCode();
    String getContent();
    AccountingDossierCategoryMode getCategoryMode();
    String getCustomCategoryName();
    Integer getDossierCategoryVersion();
    boolean isSyncCategoryRequested();
    String getCreatorId();
    AccountingDossierStatus getStatus();
    AccountingDossierStorageStatus getStorageStatus();
    Integer getRetentionYears();
    Instant getRetentionUntil();
    Instant getSubmittedAt();
    Instant getApprovedAt();
    Instant getTerminatedAt();
    Integer getReturnCount();
    boolean isActive();
    String getQrToken();
    Instant getCreatedAt();
    Instant getUpdatedAt();
    String getCreatedBy();
    String getUpdatedBy();
    
    CompanyRef getCompany();
    DepartmentRef getDepartment();
    SectionRef getSection();
    DossierCategoryRef getDossierCategory();

    interface CompanyRef {
        Long getId();
        String getCode();
        String getName();
    }

    interface DepartmentRef {
        Long getId();
        String getCode();
        String getName();
    }

    interface SectionRef {
        Long getId();
        String getCode();
        String getName();
    }

    interface DossierCategoryRef {
        Long getId();
        String getCategoryCode();
        String getCategoryName();
    }
}
