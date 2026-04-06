package vn.system.app.modules.jobpositionchart.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.UserScopeContext;
import vn.system.app.modules.department.repository.DepartmentRepository;
import vn.system.app.modules.jobpositionchart.domain.JobPositionChart;
import vn.system.app.modules.jobpositionchart.domain.response.ResJobPositionChartDTO;
import vn.system.app.modules.jobpositionchart.repository.JobPositionChartRepository;

@Service
public class JobPositionChartService {

    private final JobPositionChartRepository chartRepository;
    private final DepartmentRepository departmentRepository; // ⭐ THÊM

    public JobPositionChartService(
            JobPositionChartRepository chartRepository,
            DepartmentRepository departmentRepository) { // ⭐ THÊM
        this.chartRepository = chartRepository;
        this.departmentRepository = departmentRepository; // ⭐ THÊM
    }

    /*
     * ==========================
     * CREATE CHART
     * ==========================
     */
    public JobPositionChart handleCreateChart(JobPositionChart chart) {
        return this.chartRepository.save(chart);
    }

    /*
     * ==========================
     * DELETE CHART
     * ==========================
     */
    public void handleDeleteChart(Long id) {
        this.chartRepository.deleteById(id);
    }

    /*
     * ==========================
     * FIND BY ID
     * ==========================
     */
    public JobPositionChart fetchChartById(Long id) {
        Optional<JobPositionChart> chartOptional = this.chartRepository.findById(id);
        return chartOptional.orElse(null);
    }

    /*
     * ==========================
     * UPDATE CHART
     * ==========================
     */
    public JobPositionChart handleUpdateChart(JobPositionChart req) {

        JobPositionChart current = this.fetchChartById(req.getId());

        if (current != null) {
            current.setName(req.getName());
            current.setChartType(req.getChartType());
            current.setCompanyId(req.getCompanyId());
            current.setDepartmentId(req.getDepartmentId());

            current = this.chartRepository.save(current);
        }

        return current;
    }

    /*
     * ==========================
     * FETCH ALL WITH PAGINATION
     * ==========================
     */
    public ResultPaginationDTO fetchAllCharts(Specification<JobPositionChart> spec, Pageable pageable) {

        // ── ADMIN_SUB_2: filter theo company ──────────────
        UserScopeContext.UserScope scope = UserScopeContext.get();
        if (scope != null && !scope.isSuperAdmin()) {

            if (scope.companyIds().isEmpty()) {
                ResultPaginationDTO rs = new ResultPaginationDTO();
                ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
                mt.setPage(pageable.getPageNumber() + 1);
                mt.setPageSize(pageable.getPageSize());
                mt.setPages(0);
                mt.setTotal(0);
                rs.setMeta(mt);
                rs.setResult(List.of());
                return rs;
            }

            // Lấy tất cả departmentId thuộc company của user
            List<Long> deptIds = departmentRepository
                    .findByCompany_IdIn(scope.companyIds())
                    .stream()
                    .map(d -> d.getId())
                    .toList();

            Specification<JobPositionChart> scopeSpec = (root, query, cb) -> cb.or(
                    // chart gắn thẳng vào company
                    root.get("companyId").in(scope.companyIds()),
                    // chart gắn vào department thuộc company
                    deptIds.isEmpty()
                            ? cb.disjunction()
                            : root.get("departmentId").in(deptIds));

            spec = Specification.where(spec).and(scopeSpec);
        }
        // ── HẾT FILTER ────────────────────────────────────

        Page<JobPositionChart> page = this.chartRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(page.getTotalPages());
        mt.setTotal(page.getTotalElements());

        rs.setMeta(mt);

        List<ResJobPositionChartDTO> result = page.getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        rs.setResult(result);

        return rs;
    }

    /*
     * ==========================
     * CONVERT DTO
     * ==========================
     */
    public ResJobPositionChartDTO convertToDTO(JobPositionChart chart) {

        ResJobPositionChartDTO res = new ResJobPositionChartDTO();

        res.setId(chart.getId());
        res.setName(chart.getName());
        res.setChartType(chart.getChartType());
        res.setCompanyId(chart.getCompanyId());
        res.setDepartmentId(chart.getDepartmentId());

        return res;
    }
}