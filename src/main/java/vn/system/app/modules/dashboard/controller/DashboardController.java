package vn.system.app.modules.dashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}