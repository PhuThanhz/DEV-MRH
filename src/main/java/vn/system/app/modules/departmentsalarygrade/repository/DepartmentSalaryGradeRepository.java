package vn.system.app.modules.departmentsalarygrade.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.departmentsalarygrade.domain.DepartmentSalaryGrade;

@Repository
public interface DepartmentSalaryGradeRepository
        extends JpaRepository<DepartmentSalaryGrade, Long> {

    // Kiểm tra trùng bậc lương
    boolean existsByDepartmentJobTitleIdAndGradeLevel(
            Long departmentJobTitleId,
            Integer gradeLevel);

    // ⭐ Trả FULL danh sách bậc lương (active + inactive)
    List<DepartmentSalaryGrade> findByDepartmentJobTitleIdOrderByGradeLevelAsc(
            Long departmentJobTitleId);

    // Tìm theo gradeLevel (hỗ trợ restore)
    DepartmentSalaryGrade findByDepartmentJobTitleIdAndGradeLevel(
            Long departmentJobTitleId,
            Integer gradeLevel);

    List<DepartmentSalaryGrade> findByDepartmentJobTitleIdIn(List<Long> ids);

}
