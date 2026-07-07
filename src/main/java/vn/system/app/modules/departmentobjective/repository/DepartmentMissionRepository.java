package vn.system.app.modules.departmentobjective.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.departmentobjective.domain.DepartmentMission;

@Repository
public interface DepartmentMissionRepository extends JpaRepository<DepartmentMission, Long> {

    Optional<DepartmentMission> findByDepartmentId(Long departmentId);
}
