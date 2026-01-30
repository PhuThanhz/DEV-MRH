package vn.system.app.modules.salarygradeincome.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.salarygradeincome.domain.PayType;
import vn.system.app.modules.salarygradeincome.domain.SalaryGradeIncome;

@Repository
public interface SalaryGradeIncomeRepository
        extends JpaRepository<SalaryGradeIncome, Long>,
        JpaSpecificationExecutor<SalaryGradeIncome> {

    boolean existsBySalaryGrade_IdAndPayType(Long salaryGradeId, PayType payType);

    Optional<SalaryGradeIncome> findBySalaryGrade_IdAndPayType(
            Long salaryGradeId, PayType payType);
}
