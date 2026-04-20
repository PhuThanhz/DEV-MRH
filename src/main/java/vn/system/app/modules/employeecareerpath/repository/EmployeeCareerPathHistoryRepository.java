package vn.system.app.modules.employeecareerpath.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.employeecareerpath.domain.EmployeeCareerPathHistory;

@Repository
public interface EmployeeCareerPathHistoryRepository
        extends JpaRepository<EmployeeCareerPathHistory, Long> {

    List<EmployeeCareerPathHistory> findByEmployeeCareerPath_User_IdOrderByPromotedAtDesc(String userId);

    List<EmployeeCareerPathHistory> findByEmployeeCareerPath_IdOrderByPromotedAtDesc(Long employeeCareerPathId);
}