package vn.system.app.modules.accountingdossier.domain.request;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountingDossierSubmitRequest {
    
    private List<CustomStep> customSteps;

    @Getter
    @Setter
    public static class CustomStep {
        private String stepName;
        private int stepOrder;
        private String approverType; // DEPARTMENT_MANAGER, ACCOUNTANT, CHIEF_ACCOUNTANT, CUSTOM_USER
        private String approverUserId;
    }
}
