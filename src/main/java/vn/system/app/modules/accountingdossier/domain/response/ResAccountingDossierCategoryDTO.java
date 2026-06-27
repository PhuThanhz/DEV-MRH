package vn.system.app.modules.accountingdossier.domain.response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResAccountingDossierCategoryDTO {

    private Long id;
    private String categoryCode;
    private String categoryName;
    private String description;
    private Long companyId;
    private String scope;
    private String source;
    private Integer version;
    private boolean active;
    private List<DocumentCategoryRef> documentCategories = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @Getter
    @Setter
    public static class DocumentCategoryRef {
        private Long id;
        private String categoryCode;
        private String categoryName;
        private String symbol;
    }
}
