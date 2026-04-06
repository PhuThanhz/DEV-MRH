package vn.system.app.modules.sectionsalarygrade.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.sectionsalarygrade.domain.SectionSalaryGrade;

@Repository
public interface SectionSalaryGradeRepository
        extends JpaRepository<SectionSalaryGrade, Long> {

    boolean existsBySectionJobTitleIdAndGradeLevel(Long sectionJobTitleId, Integer gradeLevel);

    List<SectionSalaryGrade> findBySectionJobTitleIdOrderByGradeLevelAsc(Long sectionJobTitleId);

    SectionSalaryGrade findBySectionJobTitleIdAndGradeLevel(Long sectionJobTitleId, Integer gradeLevel);

    // Dùng cho SalaryMatrix + fetchByMySection
    List<SectionSalaryGrade> findBySectionJobTitleIdIn(List<Long> ids);

    // Dùng cho fetchMy (active only)
    List<SectionSalaryGrade> findBySectionJobTitleIdInAndActiveTrue(List<Long> ids);
}