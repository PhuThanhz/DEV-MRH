package vn.system.app.modules.sectionsalarygrade.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.sectionsalarygrade.domain.SectionSalaryGrade;

@Repository
public interface SectionSalaryGradeRepository
        extends JpaRepository<SectionSalaryGrade, Long> {

    // Kiểm tra tồn tại (dùng cho validate UNIQUE)
    boolean existsBySectionJobTitleIdAndGradeLevel(Long sectionJobTitleId, Integer gradeLevel);

    // Lấy danh sách active = true

    // 🔥 Thêm method này để hỗ trợ restore
    List<SectionSalaryGrade> findBySectionJobTitleIdOrderByGradeLevelAsc(Long sectionJobTitleId);

    // 🔥 Thêm method để tìm grade bất kể active (phục vụ restore)
    SectionSalaryGrade findBySectionJobTitleIdAndGradeLevel(Long sectionJobTitleId, Integer gradeLevel);

    List<SectionSalaryGrade> findBySectionJobTitleIdIn(List<Long> ids);

}
