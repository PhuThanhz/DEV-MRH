package vn.system.app.modules.salarygrade.company.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.salarygrade.company.domain.CompanyJobTitleSalaryGrade;

@Repository
public interface CompanyJobTitleSalaryGradeRepository
        extends JpaRepository<CompanyJobTitleSalaryGrade, Long> {

    boolean existsByCompanyJobTitleIdAndGradeLevel(
            Long companyJobTitleId,
            Integer gradeLevel);

    List<CompanyJobTitleSalaryGrade> findByCompanyJobTitleIdAndActiveTrueOrderByGradeLevelAsc(
            Long companyJobTitleId);
}
