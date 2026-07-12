package vn.system.app.modules.accountingdossier.domain.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResAccountingApprovalSlaSummaryDTO {
    private int overdueSteps;
    private int remindersCreated;
    private int remindersSkipped;
}
