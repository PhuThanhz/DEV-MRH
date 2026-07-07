package vn.system.app.common.util.error;

public class DuplicateInvoiceWarningException extends RuntimeException {
    public DuplicateInvoiceWarningException(String message) {
        super(message);
    }
}
