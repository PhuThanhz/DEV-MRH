package vn.system.app.modules.accountingdossier.domain.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResAccountingDossierDashboardMetricsDTO {
    private ResAccountingDossierStorageSummaryDTO summary;
    private List<ResAccountingDossierReportRowDTO> pendingByRole;
    private List<ResAccountingDossierReportRowDTO> byStatus;
    private List<ResAccountingDossierReportRowDTO> byDepartment;
    private List<ResAccountingDossierCategoryDTO> categories;
}
