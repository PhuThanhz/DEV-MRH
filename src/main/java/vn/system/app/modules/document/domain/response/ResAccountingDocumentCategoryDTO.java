package vn.system.app.modules.document.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResAccountingDocumentCategoryDTO {
    private Long id;
    private String categoryCode;
    private String categoryName;
    private String symbol;
    private String description;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
