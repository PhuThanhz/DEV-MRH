package vn.system.app.modules.jobpositionchart.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.modules.jobpositionchart.domain.JobPositionChart;
import vn.system.app.modules.jobpositionchart.domain.response.ResJobPositionChartDTO;
import vn.system.app.modules.jobpositionchart.repository.JobPositionChartRepository;

@Service
public class JobPositionChartService {

    private final JobPositionChartRepository chartRepository;

    public JobPositionChartService(JobPositionChartRepository chartRepository) {
        this.chartRepository = chartRepository;
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