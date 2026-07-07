package vn.system.app.modules.accountingdossier.domain.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class ResAccountingDossierDocumentDTO {

    private Long id;
    private Long dossierId;
    private String dossierCode;
    private String dossierContent;
    private String dossierStatus;
    private String dossierStorageStatus;
    private Ref company;
    private Ref department;
    private Ref section;
    
    private CategoryRef accountingCategory;
    private DocumentRef document;
    
    private String documentName;
    private String documentType;
    private String checkStatus;
    private String checkNote;
    private String fileUrl;
    private String externalLink;
    private Instant invoiceDate;
    private String invoiceNumber;
    private String invoiceContent;
    private String partnerName;
    private String partnerType;
    private BigDecimal amount;
    private String currency;
    private boolean active;
    private Instant deletedAt;
    private String deletedBy;
    
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @Getter
    @Setter
    public static class CategoryRef {
        private Long id;
        private String categoryCode;
        private String categoryName;
    }

    @Getter
    @Setter
    public static class DocumentRef {
        private Long id;
        private String documentCode;
        private String documentName;
    }

    @Getter
    @Setter
    public static class Ref {
        private Long id;
        private String code;
        private String name;
    }
}
