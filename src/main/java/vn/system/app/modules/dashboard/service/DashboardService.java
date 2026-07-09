package vn.system.app.modules.dashboard.service;

import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import vn.system.app.common.util.UserScopeContext;
import vn.system.app.modules.company.repository.CompanyRepository;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.repository.DepartmentRepository;
import vn.system.app.modules.section.repository.SectionRepository;
import vn.system.app.modules.dashboard.domain.response.DashboardSummaryDTO;
import vn.system.app.modules.dashboard.domain.response.DepartmentCompletenessDTO;
import vn.system.app.modules.jobpositionchart.repository.JobPositionChartRepository;
import vn.system.app.modules.departmentobjective.repository.DepartmentObjectiveRepository;
import vn.system.app.modules.departmentprocedure.repository.DepartmentProcedureRepository;
import vn.system.app.modules.permissioncategory.repository.PermissionCategoryRepository;
import vn.system.app.modules.careerpath.repository.CareerPathRepository;
import vn.system.app.modules.salarygrade.repository.SalaryGradeRepository;
import vn.system.app.modules.departmentjobtitle.repository.DepartmentJobTitleRepository;

@Service
public class DashboardService {

        private final CompanyRepository companyRepository;
        private final DepartmentRepository departmentRepository;
        private final SectionRepository sectionRepository;

        private final JobPositionChartRepository chartRepository;
        private final DepartmentObjectiveRepository objectiveRepository;
        private final DepartmentProcedureRepository departmentProcedureRepository;
        private final PermissionCategoryRepository permissionCategoryRepository;
        private final CareerPathRepository careerPathRepository;
        private final SalaryGradeRepository salaryGradeRepository;
        private final DepartmentJobTitleRepository departmentJobTitleRepository;

        public DashboardService(
                        CompanyRepository companyRepository,
                        DepartmentRepository departmentRepository,
                        SectionRepository sectionRepository,
                        JobPositionChartRepository chartRepository,
                        DepartmentObjectiveRepository objectiveRepository,
                        DepartmentProcedureRepository departmentProcedureRepository,
                        PermissionCategoryRepository permissionCategoryRepository,
                        CareerPathRepository careerPathRepository,
                        SalaryGradeRepository salaryGradeRepository,
                        DepartmentJobTitleRepository departmentJobTitleRepository) {

                this.companyRepository = companyRepository;
                this.departmentRepository = departmentRepository;
                this.sectionRepository = sectionRepository;
                this.chartRepository = chartRepository;
                this.objectiveRepository = objectiveRepository;
                this.departmentProcedureRepository = departmentProcedureRepository;
                this.permissionCategoryRepository = permissionCategoryRepository;
                this.careerPathRepository = careerPathRepository;
                this.salaryGradeRepository = salaryGradeRepository;
                this.departmentJobTitleRepository = departmentJobTitleRepository;
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

                // SUPER_ADMIN và ADMIN_SUB_1 đều thấy toàn bộ hệ thống
                if (scope == null || scope.isSuperAdmin() || scope.isAdminLevel()) {
                        totalCompany = companyRepository.count();
                        totalDepartment = departmentRepository.count();
                        totalSection = sectionRepository.count();
                } else if (scope.isDepartmentLevel()) {
                        var departmentIds = scope.departmentIds();
                        if (departmentIds == null || departmentIds.isEmpty()) {
                                return new DashboardSummaryDTO(0, 0, 0);
                        }
                        totalCompany = scope.companyIds() != null ? scope.companyIds().size() : 0;
                        totalDepartment = departmentIds.size();
                        totalSection = sectionRepository.countByDepartment_IdIn(departmentIds);
                } else {
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

        /*
         * ====================================================
         * DEPARTMENT COMPLETENESS
         * ====================================================
         */
        public List<DepartmentCompletenessDTO> getDepartmentCompleteness() {
                return getDepartmentCompleteness(null, null, null);
        }

        public List<DepartmentCompletenessDTO> getDepartmentCompleteness(String search, String companyName,
                        String status) {

                UserScopeContext.UserScope scope = UserScopeContext.get();

                List<Department> departments;

                // SUPER_ADMIN và ADMIN_SUB_1 đều thấy toàn bộ hệ thống
                if (scope == null || scope.isSuperAdmin() || scope.isAdminLevel()) {
                        departments = departmentRepository.findAll();
                } else if (scope.isDepartmentLevel()) {
                        var departmentIds = scope.departmentIds();
                        if (departmentIds == null || departmentIds.isEmpty()) {
                                return List.of();
                        }
                        departments = departmentRepository.findByIdIn(departmentIds);
                } else {
                        var companyIds = scope.companyIds();
                        if (companyIds.isEmpty()) {
                                return List.of();
                        }
                        departments = departmentRepository.findByCompany_IdIn(companyIds);
                }

                List<Department> filteredDepts = departments.stream()
                                .filter(dept -> matchesSearch(dept, search))
                                .filter(dept -> matchesCompany(dept, companyName))
                                .collect(Collectors.toList());

                List<Long> deptIds = filteredDepts.stream().map(Department::getId).collect(Collectors.toList());

                Set<Long> orgCharts = deptIds.isEmpty() ? Collections.emptySet() : chartRepository.findDepartmentIdsWithChart(deptIds);
                Set<Long> objectives = deptIds.isEmpty() ? Collections.emptySet() : objectiveRepository.findDepartmentIdsWithObjectives(deptIds);
                Set<Long> procedures = deptIds.isEmpty() ? Collections.emptySet() : departmentProcedureRepository.findDepartmentIdsWithProcedure(deptIds);
                Set<Long> permissions = deptIds.isEmpty() ? Collections.emptySet() : permissionCategoryRepository.findDepartmentIdsWithPermissions(deptIds);
                Set<Long> careerPaths = deptIds.isEmpty() ? Collections.emptySet() : careerPathRepository.findDepartmentIdsWithCareerPath(deptIds);
                Set<Long> salaryGrades = deptIds.isEmpty() ? Collections.emptySet() : salaryGradeRepository.findDepartmentIdsWithSalaryGrade(deptIds);
                Set<Long> jobTitleMaps = deptIds.isEmpty() ? Collections.emptySet() : departmentJobTitleRepository.findDepartmentIdsWithJobTitleMap(deptIds);

                return filteredDepts.stream()
                                .map(dept -> buildCompleteness(dept, orgCharts, objectives, procedures, permissions, careerPaths, salaryGrades, jobTitleMaps))
                                .filter(dto -> matchesStatus(dto, status))
                                .collect(Collectors.toList());
        }

        private boolean matchesSearch(Department dept, String search) {
                if (isBlank(search)) {
                        return true;
                }
                String keyword = normalize(search);
                return normalize(dept.getName()).contains(keyword)
                                || normalize(dept.getCode()).contains(keyword)
                                || (dept.getCompany() != null
                                                 && normalize(dept.getCompany().getName()).contains(keyword));
        }

        private boolean matchesCompany(Department dept, String companyName) {
                if (isBlank(companyName)) {
                        return true;
                }
                return dept.getCompany() != null
                                && normalize(dept.getCompany().getName()).equals(normalize(companyName));
        }

        private boolean matchesStatus(DepartmentCompletenessDTO dto, String status) {
                if (isBlank(status) || "all".equalsIgnoreCase(status)) {
                        return true;
                }
                return switch (status.toLowerCase()) {
                        case "full" -> dto.getScore() == 7;
                        case "partial" -> dto.getScore() > 0 && dto.getScore() < 7;
                        case "empty" -> dto.getScore() == 0;
                        default -> true;
                };
        }

        private boolean isBlank(String value) {
                return value == null || value.trim().isEmpty();
        }

        private String normalize(String value) {
                return value == null ? "" : value.trim().toLowerCase();
        }

        private DepartmentCompletenessDTO buildCompleteness(
                        Department dept,
                        Set<Long> orgCharts,
                        Set<Long> objectives,
                        Set<Long> procedures,
                        Set<Long> permissions,
                        Set<Long> careerPaths,
                        Set<Long> salaryGrades,
                        Set<Long> jobTitleMaps) {

                Long deptId = dept.getId();

                boolean orgChart = orgCharts.contains(deptId);
                boolean hasObjectives = objectives.contains(deptId);
                boolean departmentProcedure = procedures.contains(deptId);
                boolean hasPermissions = permissions.contains(deptId);
                boolean careerPath = careerPaths.contains(deptId);
                boolean salaryGrade = salaryGrades.contains(deptId);
                boolean jobTitleMap = jobTitleMaps.contains(deptId);

                int score = (orgChart ? 1 : 0)
                                + (hasObjectives ? 1 : 0)
                                + (departmentProcedure ? 1 : 0)
                                + (hasPermissions ? 1 : 0)
                                + (careerPath ? 1 : 0)
                                + (salaryGrade ? 1 : 0)
                                + (jobTitleMap ? 1 : 0);

                return new DepartmentCompletenessDTO(
                                deptId,
                                dept.getName(),
                                dept.getCompany() != null ? dept.getCompany().getName() : "",
                                orgChart,
                                hasObjectives,
                                departmentProcedure,
                                hasPermissions,
                                careerPath,
                                salaryGrade,
                                jobTitleMap,
                                score);
        }
}
