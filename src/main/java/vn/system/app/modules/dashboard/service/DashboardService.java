package vn.system.app.modules.dashboard.service;

import java.util.List;
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

                if (scope == null || scope.isSuperAdmin()) {
                        totalCompany = companyRepository.count();
                        totalDepartment = departmentRepository.count();
                        totalSection = sectionRepository.count();
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

                UserScopeContext.UserScope scope = UserScopeContext.get();
                // THÊM LOG
                System.out.println(">>> SCOPE = " + scope);
                if (scope != null) {
                        System.out.println(">>> isSuperAdmin = " + scope.isSuperAdmin());
                        System.out.println(">>> companyIds = " + scope.companyIds());
                }
                List<Department> departments;

                if (scope == null || scope.isSuperAdmin()) {
                        departments = departmentRepository.findAll();
                } else {
                        var companyIds = scope.companyIds();
                        if (companyIds.isEmpty()) {
                                return List.of();
                        }
                        departments = departmentRepository.findByCompany_IdIn(companyIds);
                }
                System.out.println(">>> departments.size() = " + departments.size());

                return departments.stream()
                                .map(this::buildCompleteness)
                                .collect(Collectors.toList());
        }

        private DepartmentCompletenessDTO buildCompleteness(Department dept) {

                Long deptId = dept.getId();

                boolean orgChart = !chartRepository
                                .findAll()
                                .stream()
                                .filter(c -> deptId.equals(c.getDepartmentId()))
                                .toList()
                                .isEmpty();

                boolean objectives = !objectiveRepository
                                .findByDepartmentId(deptId)
                                .isEmpty();

                boolean departmentProcedure = !departmentProcedureRepository
                                .findByDepartment_Id(deptId)
                                .isEmpty();

                boolean permissions = !permissionCategoryRepository
                                .findByDepartmentId(deptId)
                                .isEmpty();

                boolean careerPath = !careerPathRepository
                                .findByDepartment_IdAndActiveTrue(deptId)
                                .isEmpty();

                boolean salaryGrade = !salaryGradeRepository
                                .findAll()
                                .stream()
                                .filter(sg -> "DEPARTMENT".equals(sg.getContextType())
                                                && deptId.equals(sg.getContextId())
                                                && sg.isActive())
                                .toList()
                                .isEmpty();

                boolean jobTitleMap = !departmentJobTitleRepository
                                .findByDepartment_IdAndActiveTrue(deptId)
                                .isEmpty();

                int score = (orgChart ? 1 : 0)
                                + (objectives ? 1 : 0)
                                + (departmentProcedure ? 1 : 0)
                                + (permissions ? 1 : 0)
                                + (careerPath ? 1 : 0)
                                + (salaryGrade ? 1 : 0)
                                + (jobTitleMap ? 1 : 0);

                return new DepartmentCompletenessDTO(
                                deptId,
                                dept.getName(),
                                dept.getCompany().getName(), // ← THÊM
                                orgChart,
                                objectives,
                                departmentProcedure,
                                permissions,
                                careerPath,
                                salaryGrade,
                                jobTitleMap,
                                score);
        }
}