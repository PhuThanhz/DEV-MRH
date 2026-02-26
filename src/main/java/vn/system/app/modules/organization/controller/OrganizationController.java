package vn.system.app.modules.organization.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.organization.domain.response.ResOrganizationChartDTO;
import vn.system.app.modules.organization.service.DepartmentOrganizationService;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

    private final DepartmentOrganizationService departmentOrganizationService;

    public OrganizationController(
            DepartmentOrganizationService departmentOrganizationService) {

        this.departmentOrganizationService = departmentOrganizationService;
    }

    /*
     * =====================================================
     * LẤY SƠ ĐỒ TỔ CHỨC PHÒNG BAN
     * =====================================================
     */
    @GetMapping("/departments/{departmentId}")
    @ApiMessage("Sơ đồ tổ chức phòng ban")
    public ResponseEntity<ResOrganizationChartDTO> getDepartmentOrganization(
            @PathVariable("departmentId") Long departmentId) {

        ResOrganizationChartDTO response = departmentOrganizationService.buildDepartmentOrganization(departmentId);

        return ResponseEntity.ok(response);
    }

}