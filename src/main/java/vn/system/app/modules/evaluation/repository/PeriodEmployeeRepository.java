package vn.system.app.modules.evaluation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.evaluation.domain.PeriodEmployee;
import vn.system.app.modules.evaluation.domain.enums.PeriodEmployeeStatus;

@Repository
public interface PeriodEmployeeRepository extends JpaRepository<PeriodEmployee, Long> {

    List<PeriodEmployee> findByPeriodId(Long periodId);

    List<PeriodEmployee> findByPeriodIdAndStatus(Long periodId, PeriodEmployeeStatus status);

    Optional<PeriodEmployee> findByPeriodIdAndEmployeeId(Long periodId, String employeeId);

    boolean existsByPeriodIdAndEmployeeId(Long periodId, String employeeId);

    /** Tìm nhân viên theo quản lý trực tiếp trong kỳ */
    List<PeriodEmployee> findByPeriodIdAndDirectManagerId(Long periodId, String directManagerId);

    /** Tìm nhân viên theo quản lý gián tiếp trong kỳ */
    List<PeriodEmployee> findByPeriodIdAndIndirectManagerId(Long periodId, String indirectManagerId);
}
