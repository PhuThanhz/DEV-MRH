package vn.system.app.modules.accountingdossier.domain.request;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class AccountingDossierValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testAccountingDossierDocumentRequest_Valid() {
        AccountingDossierDocumentRequest req = new AccountingDossierDocumentRequest();
        req.setAccountingCategoryId(1L);
        req.setDocumentName("Hóa đơn mua hàng");
        req.setAmount(new BigDecimal("1500000.00"));
        req.setFileUrl("documents/invoice_01.pdf");
        req.setExternalLink("https://invoice-verify.com/check/123");
        req.setInvoiceDate(Instant.now().minus(1, ChronoUnit.DAYS));

        Set<ConstraintViolation<AccountingDossierDocumentRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty(), "Valid request should have no validation violations");
    }

    @Test
    void testAccountingDossierDocumentRequest_InvalidAmount() {
        AccountingDossierDocumentRequest req = new AccountingDossierDocumentRequest();
        req.setAccountingCategoryId(1L);
        req.setDocumentName("Hóa đơn lỗi số tiền");
        req.setAmount(new BigDecimal("-100.00")); // Âm

        Set<ConstraintViolation<AccountingDossierDocumentRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty(), "Negative amount should trigger validation violation");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void testAccountingDossierDocumentRequest_InvalidExternalLink() {
        AccountingDossierDocumentRequest req = new AccountingDossierDocumentRequest();
        req.setAccountingCategoryId(1L);
        req.setDocumentName("Hóa đơn link lỗi");
        req.setExternalLink("javascript:alert('xss')"); // Không hợp lệ

        Set<ConstraintViolation<AccountingDossierDocumentRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty(), "Invalid external link should trigger validation violation");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("externalLink")));
    }

    @Test
    void testAccountingDossierDocumentRequest_CaseInsensitiveExternalLink() {
        AccountingDossierDocumentRequest req = new AccountingDossierDocumentRequest();
        req.setAccountingCategoryId(1L);
        req.setDocumentName("Hóa đơn link hoa");
        req.setExternalLink("HTTPS://MYLINK.COM"); // Viết hoa, phải hợp lệ nhờ (?i)

        Set<ConstraintViolation<AccountingDossierDocumentRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty(), "Case-insensitive HTTP/HTTPS protocol in link should be valid");
    }

    @Test
    void testAccountingDossierDocumentRequest_FutureInvoiceDate() {
        AccountingDossierDocumentRequest req = new AccountingDossierDocumentRequest();
        req.setAccountingCategoryId(1L);
        req.setDocumentName("Hóa đơn tương lai");
        req.setInvoiceDate(Instant.now().plus(5, ChronoUnit.DAYS)); // Tương lai

        Set<ConstraintViolation<AccountingDossierDocumentRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty(), "Future invoice date should trigger validation violation");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("invoiceDate")));
    }

    @Test
    void testAccountingDossierSubmitRequest_Valid() {
        AccountingDossierSubmitRequest req = new AccountingDossierSubmitRequest();
        List<AccountingDossierSubmitRequest.CustomStep> steps = new ArrayList<>();
        AccountingDossierSubmitRequest.CustomStep step = new AccountingDossierSubmitRequest.CustomStep();
        step.setStepKey("STEP_01");
        step.setStepName("Phê duyệt phòng ban");
        step.setStepOrder(1);
        step.setApproverType("DEPARTMENT_MANAGER");
        steps.add(step);
        req.setCustomSteps(steps);

        Set<ConstraintViolation<AccountingDossierSubmitRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty(), "Valid submit request should have no validation violations");
    }

    @Test
    void testAccountingDossierSubmitRequest_InvalidCustomStep() {
        AccountingDossierSubmitRequest req = new AccountingDossierSubmitRequest();
        List<AccountingDossierSubmitRequest.CustomStep> steps = new ArrayList<>();
        AccountingDossierSubmitRequest.CustomStep step = new AccountingDossierSubmitRequest.CustomStep();
        step.setStepKey(""); // Trống
        step.setStepOrder(0); // Nhỏ hơn 1
        step.setApproverType(""); // Trống
        steps.add(step);
        req.setCustomSteps(steps);

        Set<ConstraintViolation<AccountingDossierSubmitRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty(), "Invalid fields in CustomStep should trigger validation violations");
    }
}
