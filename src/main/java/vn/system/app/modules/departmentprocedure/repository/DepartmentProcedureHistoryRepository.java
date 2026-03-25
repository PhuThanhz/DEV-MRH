package vn.system.app.modules.departmentprocedure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.departmentprocedure.domain.DepartmentProcedureHistory;

@Repository
public interface DepartmentProcedureHistoryRepository
        extends JpaRepository<DepartmentProcedureHistory, Long> {

    List<DepartmentProcedureHistory> findByProcedure_IdOrderByVersionDesc(Long procedureId);
}