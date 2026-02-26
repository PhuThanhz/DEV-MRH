package vn.system.app.modules.jd.positionchart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jd.positionchart.domain.JDPositionChart;

@Repository
public interface JDPositionChartRepository extends JpaRepository<JDPositionChart, Long> {

    // Dùng trong JobDescriptionService.update để xoá và replace chart
    void deleteByJobDescriptionId(Long jobDescriptionId);

    // Dùng trong JDPositionChartService.getGraph
    List<JDPositionChart> findByJobDescriptionIdAndActiveTrue(Long jobDescriptionId);

    // Dùng nếu bạn muốn lấy list đã sort (khuyên dùng cho full detail)
    List<JDPositionChart> findByJobDescriptionIdAndActiveTrueOrderBySortOrderAsc(Long jobDescriptionId);
}