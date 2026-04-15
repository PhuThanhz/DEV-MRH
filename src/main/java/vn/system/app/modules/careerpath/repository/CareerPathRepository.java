package vn.system.app.modules.careerpath.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.careerpath.domain.CareerPath;

@Repository
public interface CareerPathRepository extends JpaRepository<CareerPath, Long> {

    boolean existsByDepartment_IdAndJobTitle_Id(Long departmentId, Long jobTitleId);

    // Lộ trình theo phòng ban -> sort bandOrder DESC
    List<CareerPath> findByDepartment_IdOrderByJobTitle_PositionLevel_BandOrderDesc(Long departmentId);

    // Lấy toàn bộ active
    List<CareerPath> findByActiveTrue();

    // Lấy active theo phòng ban
    List<CareerPath> findByDepartment_IdAndActiveTrue(Long departmentId);

    // ⭐ Tối ưu Global sort: bandOrder desc, level asc
    List<CareerPath> findByDepartment_IdOrderByJobTitle_PositionLevel_BandOrderDescJobTitle_PositionLevel_CodeAsc(
            Long departmentId);

    // ⭐ Fix N+1: lấy toàn bộ jobTitleId đã có trong department bằng 1 query duy
    // nhất
    @Query("SELECT cp.jobTitle.id FROM CareerPath cp WHERE cp.department.id = :departmentId")
    Set<Long> findExistingJobTitleIds(@Param("departmentId") Long departmentId);
}