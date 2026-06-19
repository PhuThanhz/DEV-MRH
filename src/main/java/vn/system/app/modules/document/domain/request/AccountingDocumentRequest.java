package vn.system.app.modules.document.domain.request;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountingDocumentRequest {

    @Size(max = 100, message = "Mã chứng từ tối đa 100 ký tự")
    private String documentCode;

    @NotBlank(message = "Nội dung chứng từ không được để trống")
    @Size(max = 250, message = "Nội dung chứng từ tối đa 250 ký tự")
    private String documentName;

    private Instant issuedDate;
    private List<String> fileUrls;
    private String note;
    private Long folderId;
    private Long accountingCategoryId;

    public DocumentRequest toDocumentRequest(Long accountingCategoryId, String fallbackDocumentCode) {
        DocumentRequest req = new DocumentRequest();
        req.setDocumentCode(
                documentCode == null || documentCode.trim().isEmpty()
                        ? fallbackDocumentCode
                        : documentCode);
        req.setDocumentName(documentName);
        req.setCategoryId(accountingCategoryId);
        req.setIssuedDate(issuedDate);
        req.setFileUrls(fileUrls);
        req.setNote(note);
        req.setFolderId(folderId);
        req.setAccountingCategoryId(this.accountingCategoryId);
        return req;
    }
}
