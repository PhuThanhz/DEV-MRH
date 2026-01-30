package vn.system.app.modules.careerpath.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.careerpath.domain.CareerPath;

@Repository
public interface CareerPathRepository extends JpaRepository<CareerPath, Long> {

    boolean existsByDepartment_IdAndJobTitle_Id(Long departmentId, Long jobTitleId);

    // sắp xếp theo bandOrder (cao → thấp)
    List<CareerPath> findByDepartment_IdOrderByJobTitle_PositionLevel_BandOrderDesc(Long departmentId);
}
