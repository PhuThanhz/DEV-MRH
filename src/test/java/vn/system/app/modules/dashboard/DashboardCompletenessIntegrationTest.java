package vn.system.app.modules.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.UserScopeContext;
import vn.system.app.modules.dashboard.service.DashboardService;

@SpringBootTest
@Transactional(readOnly = true)
class DashboardCompletenessIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Test
    void overviewAndDatabaseFiltersExecuteWithNativeProjection() {
        UserScopeContext.clear();

        var overview = dashboardService.getDepartmentCompletenessOverview();
        ResultPaginationDTO filtered = dashboardService.getDepartmentCompleteness(
                null, null, "partial", "orgChart", PageRequest.of(0, 5));

        assertThat(overview.total()).isGreaterThanOrEqualTo(0);
        assertThat(overview.full() + overview.partial() + overview.empty()).isEqualTo(overview.total());
        assertThat(overview.topMissing()).hasSizeLessThanOrEqualTo(5);
        assertThat(filtered.getMeta().getPageSize()).isEqualTo(5);
        assertThat(filtered.getResult()).isInstanceOf(java.util.List.class);
    }
}
