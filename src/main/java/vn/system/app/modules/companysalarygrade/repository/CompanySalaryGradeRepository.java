package vn.system.app.modules.companysalarygrade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.system.app.modules.companysalarygrade.domain.CompanySalaryGrade;

import java.util.List;

@Repository
public interface CompanySalaryGradeRepository
                extends JpaRepository<CompanySalaryGrade, Long> {

        boolean existsByCompanyJobTitleIdAndGradeLevel(Long companyJobTitleId, Integer gradeLevel);

        List<CompanySalaryGrade> findByCompanyJobTitleIdOrderByGradeLevelAsc(Long companyJobTitleId);

        CompanySalaryGrade findByCompanyJobTitleIdAndGradeLevel(Long companyJobTitleId, Integer gradeLevel);

        // Dùng cho fetch toàn công ty (admin/HR công ty)
        List<CompanySalaryGrade> findByCompanyJobTitleIdIn(List<Long> ids);

        // Dùng cho fetch cá nhân (active only)
        List<CompanySalaryGrade> findByCompanyJobTitleIdInAndActiveTrue(List<Long> ids);
}