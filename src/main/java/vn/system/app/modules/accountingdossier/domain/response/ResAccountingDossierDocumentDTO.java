package vn.system.app.modules.accountingdossier.domain.response;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class ResAccountingDossierDocumentDTO {

    private Long id;
    private Long dossierId;
    
    private CategoryRef accountingCategory;
    private DocumentRef document;
    
    private String documentName;
    private String documentType;
    private String checkStatus;
    private String checkNote;
    private boolean active;
    
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
}
