package vn.system.app.modules.accountingdossier.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class AccountingDossierDocumentRequest {

    @NotNull(message = "Danh mục loại chứng từ không được để trống")
    private Long accountingCategoryId;

    @NotBlank(message = "Tên chứng từ không được để trống")
    private String documentName;

    private String documentType; // Kept for existing data/imports. Defaults to OTHER when omitted.

    private Long documentId; // Optional, if using existing document table

    private String fileUrl;

    private String externalLink;

    private Instant invoiceDate;

    private String invoiceNumber;

    private String invoiceContent;

    private String partnerName;

    private String partnerType;

    private BigDecimal amount;

    private String currency;

    private Boolean confirmDuplicate;

    private String duplicateReason;
}
