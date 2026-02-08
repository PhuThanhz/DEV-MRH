package vn.system.app.modules.companysalarygrade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.system.app.modules.companysalarygrade.domain.CompanySalaryGrade;

import java.util.List;

@Repository
public interface CompanySalaryGradeRepository
                extends JpaRepository<CompanySalaryGrade, Long> {

        // Kiểm tra trùng bậc lương
        boolean existsByCompanyJobTitleIdAndGradeLevel(Long companyJobTitleId, Integer gradeLevel);

        // ⛔ Trả ra TẤT CẢ (active + inactive) theo thứ tự level
        List<CompanySalaryGrade> findByCompanyJobTitleIdOrderByGradeLevelAsc(Long companyJobTitleId);

        // Tìm 1 bản ghi theo jobTitle + gradeLevel (phục vụ restore)
        CompanySalaryGrade findByCompanyJobTitleIdAndGradeLevel(Long companyJobTitleId, Integer gradeLevel);

        List<CompanySalaryGrade> findByCompanyJobTitleIdIn(List<Long> ids);

}
