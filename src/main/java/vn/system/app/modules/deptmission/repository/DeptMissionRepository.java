package vn.system.app.modules.deptmission.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.deptmission.domain.DeptMission;

@Repository
public interface DeptMissionRepository
        extends JpaRepository<DeptMission, Long> {

    List<DeptMission> findAllByDepartmentId(Long departmentId);

    Optional<DeptMission> findByDepartmentIdAndSectionId(
            Long departmentId,
            Long sectionId);

    void deleteAllByDepartmentId(Long departmentId);

    boolean existsByDepartmentId(Long departmentId);
}
