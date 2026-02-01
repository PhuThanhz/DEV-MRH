package vn.system.app.modules.careerpath.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.careerpath.domain.CareerPath;

@Repository
public interface CareerPathRepository extends JpaRepository<CareerPath, Long> {

    // Kiểm tra trùng lặp lộ trình trong cùng phòng ban
    boolean existsByDepartment_IdAndJobTitle_Id(Long departmentId, Long jobTitleId);

    // Lấy danh sách theo phòng ban, sắp xếp theo thứ tự band (cao → thấp)
    List<CareerPath> findByDepartment_IdOrderByJobTitle_PositionLevel_BandOrderDesc(Long departmentId);

    // Lấy danh sách toàn bộ lộ trình đang active (nếu cần dùng trong dropdown)
    List<CareerPath> findByActiveTrue();

}
