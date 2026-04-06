package vn.system.app.modules.dashboard.service;

import org.springframework.stereotype.Service;

import vn.system.app.common.util.UserScopeContext;
import vn.system.app.modules.company.repository.CompanyRepository;
import vn.system.app.modules.department.repository.DepartmentRepository;
import vn.system.app.modules.section.repository.SectionRepository;
import vn.system.app.modules.dashboard.domain.response.DashboardSummaryDTO;

@Service
public class DashboardService {

    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final SectionRepository sectionRepository;

    public DashboardService(
            CompanyRepository companyRepository,
            DepartmentRepository departmentRepository,
            SectionRepository sectionRepository) {

        this.companyRepository = companyRepository;
        this.departmentRepository = departmentRepository;
        this.sectionRepository = sectionRepository;
    }

    /*
     * ====================================================
     * DASHBOARD SUMMARY (KPI)
     * ====================================================
     */
    public DashboardSummaryDTO getSummary() {

        UserScopeContext.UserScope scope = UserScopeContext.get();

        long totalCompany;
        long totalDepartment;
        long totalSection;

        if (scope == null || scope.isSuperAdmin()) {
            // SUPER_ADMIN thấy tất cả
            totalCompany = companyRepository.count();
            totalDepartment = departmentRepository.count();
            totalSection = sectionRepository.count();
        } else {
            // ADMIN_SUB_2 chỉ thấy theo company của mình
            var companyIds = scope.companyIds();
            if (companyIds.isEmpty()) {
                return new DashboardSummaryDTO(0, 0, 0);
            }
            totalCompany = companyIds.size();
            totalDepartment = departmentRepository.countByCompany_IdIn(companyIds);
            totalSection = sectionRepository.countByDepartment_Company_IdIn(companyIds);
        }

        return new DashboardSummaryDTO(totalCompany, totalDepartment, totalSection);
    }
}