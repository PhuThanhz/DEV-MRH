package vn.system.app.modules.accountingdossier.domain.request;

import jakarta.validation.constraints.*;
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
    @Size(max = 250, message = "Tên chứng từ không được vượt quá 250 ký tự")
    private String documentName;

    private String documentType; // Kept for existing data/imports. Defaults to OTHER when omitted.

    private Long documentId; // Optional, if using existing document table

    @Size(max = 1000, message = "Đường dẫn file không được vượt quá 1000 ký tự")
    private String fileUrl;

    @Size(max = 1000, message = "Đường dẫn liên kết ngoài không được vượt quá 1000 ký tự")
    @Pattern(regexp = "(?i)^(https?://.*)?$", message = "Đường dẫn liên kết ngoài phải bắt đầu bằng http:// hoặc https://")
    private String externalLink;

    @PastOrPresent(message = "Ngày hóa đơn không được ở tương lai")
    private Instant invoiceDate;

    private String invoiceNumber;

    private String invoiceContent;

    private String partnerName;

    private String partnerType;

    @PositiveOrZero(message = "Số tiền phải lớn hơn hoặc bằng 0")
    @Digits(integer = 16, fraction = 2, message = "Số tiền không hợp lệ (tối đa 16 chữ số phần nguyên và 2 chữ số phần thập phân)")
    private BigDecimal amount;

    private String currency;

    private Boolean confirmDuplicate;

    private String duplicateReason;
}
