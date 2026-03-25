package vn.system.app.modules.dashboard.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryDTO {

    private long totalCompany;
    private long totalDepartment;
    private long totalSection;
}