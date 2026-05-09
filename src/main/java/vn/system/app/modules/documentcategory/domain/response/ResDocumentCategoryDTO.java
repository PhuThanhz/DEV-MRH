package vn.system.app.modules.documentcategory.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResDocumentCategoryDTO {

    private Long id;
    private String categoryCode;
    private String categoryName;
    private String symbol;
    private String definition;
    private boolean active;
    private boolean mappingProcedure;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}