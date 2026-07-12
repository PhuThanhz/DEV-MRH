package vn.system.app.modules.accountingdossier.domain.enums;

public enum ApproverType {
    DEPARTMENT_MANAGER,
    ACCOUNTANT,
    CHIEF_ACCOUNTANT,
    DIRECTOR,
    CUSTOM;

    public static ApproverType fromString(String val) {
        if (val == null) return null;
        String upper = val.trim().toUpperCase();
        switch (upper) {
            case "ACOUNTANT":
            case "ACCOUNTANT":
                return ACCOUNTANT;
            case "CHIEF_ACOUNTANT":
            case "CHIEF_ACCOUNTANT":
                return CHIEF_ACCOUNTANT;
            case "DEPARTMENT_MANAGER":
                return DEPARTMENT_MANAGER;
            case "DIRECTOR":
                return DIRECTOR;
            case "CUSTOM":
            case "CUSTOM_USER":
                return CUSTOM;
            default:
                throw new IllegalArgumentException("Unknown ApproverType: " + val);
        }
    }
}
