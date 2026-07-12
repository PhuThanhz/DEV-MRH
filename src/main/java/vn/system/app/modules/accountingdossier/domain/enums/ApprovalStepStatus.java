package vn.system.app.modules.accountingdossier.domain.enums;

public enum ApprovalStepStatus {
    PENDING,
    CURRENT,
    APPROVED,
    REJECTED,
    RETURNED,
    SKIPPED,
    SKIPPED_CONFLICT,
    SKIPPED_DUPLICATE,
    CANCELLED;

    public static ApprovalStepStatus fromString(String val) {
        if (val == null) return null;
        String upper = val.trim().toUpperCase();
        try {
            return ApprovalStepStatus.valueOf(upper);
        } catch (IllegalArgumentException e) {
            if ("SKIPPED".equals(upper)) return SKIPPED;
            throw new IllegalArgumentException("Unknown ApprovalStepStatus: " + val);
        }
    }
}
