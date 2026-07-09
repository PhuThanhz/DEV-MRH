package vn.system.app.modules.salarygrade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.system.app.modules.salarygrade.domain.SalaryGrade;

@Repository
public interface SalaryGradeRepository
        extends JpaRepository<SalaryGrade, Long>, JpaSpecificationExecutor<SalaryGrade> {

    boolean existsByContextTypeAndContextIdAndGradeLevel(String contextType, Long contextId, Integer gradeLevel);

    boolean existsByContextTypeAndContextIdAndActiveTrue(String contextType, Long contextId);

    @Query("SELECT DISTINCT sg.contextId FROM SalaryGrade sg WHERE sg.contextType = 'DEPARTMENT' AND sg.active = true AND sg.contextId IN :contextIds")
    java.util.Set<Long> findDepartmentIdsWithSalaryGrade(@Param("contextIds") java.util.Collection<Long> contextIds);
}