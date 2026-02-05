package vn.system.app.modules.companysalarygrade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.system.app.modules.companysalarygrade.domain.CompanySalaryGrade;

import java.util.List;

@Repository
public interface CompanySalaryGradeRepository
                extends JpaRepository<CompanySalaryGrade, Long> {

        boolean existsByCompanyJobTitleIdAndGradeLevel(Long companyJobTitleId, Integer gradeLevel);

        List<CompanySalaryGrade> findByCompanyJobTitleIdAndActiveTrueOrderByGradeLevelAsc(Long companyJobTitleId);
}
