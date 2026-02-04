package vn.system.app.modules.salarygrade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.salarygrade.domain.SalaryGrade;

@Repository
public interface SalaryGradeRepository
        extends JpaRepository<SalaryGrade, Long>, JpaSpecificationExecutor<SalaryGrade> {

    boolean existsByContextTypeAndContextIdAndGradeLevel(String contextType, Long contextId, Integer gradeLevel);
}