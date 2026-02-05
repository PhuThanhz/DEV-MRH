package vn.system.app.modules.departmentsalarygrade.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.departmentsalarygrade.domain.DepartmentSalaryGrade;

@Repository
public interface DepartmentSalaryGradeRepository
        extends JpaRepository<DepartmentSalaryGrade, Long> {

    boolean existsByDepartmentJobTitleIdAndGradeLevel(
            Long departmentJobTitleId,
            Integer gradeLevel);

    List<DepartmentSalaryGrade> findByDepartmentJobTitleIdAndActiveTrueOrderByGradeLevelAsc(
            Long departmentJobTitleId);
}
