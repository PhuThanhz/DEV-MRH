package vn.system.app.modules.salarygradeperformancerating.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.salarygradeperformancerating.domain.SalaryGradePerformanceRating;

@Repository
public interface SalaryGradePerformanceRatingRepository
        extends JpaRepository<SalaryGradePerformanceRating, Long>,
        JpaSpecificationExecutor<SalaryGradePerformanceRating> {

    Optional<SalaryGradePerformanceRating> findBySalaryGrade_Id(Long salaryGradeId);

    boolean existsBySalaryGrade_Id(Long salaryGradeId);
}
