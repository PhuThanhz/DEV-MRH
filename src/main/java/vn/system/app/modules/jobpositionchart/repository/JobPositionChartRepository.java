package vn.system.app.modules.jobpositionchart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jobpositionchart.domain.JobPositionChart;

@Repository
public interface JobPositionChartRepository
                extends JpaRepository<JobPositionChart, Long>, JpaSpecificationExecutor<JobPositionChart> {

        List<JobPositionChart> findByDepartmentId(Long departmentId);

        @org.springframework.data.jpa.repository.Query("SELECT DISTINCT c.departmentId FROM JobPositionChart c WHERE c.departmentId IN :departmentIds")
        java.util.Set<Long> findDepartmentIdsWithChart(@org.springframework.data.repository.query.Param("departmentIds") java.util.Collection<Long> departmentIds);
}