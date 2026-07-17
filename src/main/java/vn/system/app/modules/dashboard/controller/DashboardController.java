package vn.system.app.modules.dashboard.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.dashboard.domain.response.DashboardSummaryDTO;
import vn.system.app.modules.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /*
     * ====================================================
     * GET DASHBOARD SUMMARY
     * ====================================================
     */
    @GetMapping("/summary")
    @ApiMessage("Lấy dữ liệu tổng quan dashboard")
    public ResponseEntity<DashboardSummaryDTO> getSummary() {
        return ResponseEntity.ok(
                this.dashboardService.getSummary());
    }

    /*
     * ====================================================
     * GET DEPARTMENT COMPLETENESS
     * ====================================================
     */
    @GetMapping("/department-completeness")
    @ApiMessage("Kiểm tra mức độ hoàn thiện cấu hình các phòng ban")
    public ResponseEntity<?> getDepartmentCompleteness(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String missing,
            @RequestParam(defaultValue = "false") boolean overview,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        if (overview) {
            return ResponseEntity.ok(dashboardService.getDepartmentCompletenessOverview());
        }
        return ResponseEntity.ok(
                this.dashboardService.getDepartmentCompleteness(search, companyName, status, missing, pageable));
    }
}
