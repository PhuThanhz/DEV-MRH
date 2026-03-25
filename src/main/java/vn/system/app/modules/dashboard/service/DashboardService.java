package vn.system.app.modules.dashboard.service;

import org.springframework.stereotype.Service;

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

        long totalCompany = companyRepository.count();
        long totalDepartment = departmentRepository.count();
        long totalSection = sectionRepository.count();

        return new DashboardSummaryDTO(
                totalCompany,
                totalDepartment,
                totalSection);
    }
}