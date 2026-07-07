package vn.system.app.modules.departmentobjective.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.system.app.modules.departmentobjective.domain.DepartmentMissionVersion;

public interface DepartmentMissionVersionRepository extends JpaRepository<DepartmentMissionVersion, Long> {

    List<DepartmentMissionVersion> findByDepartmentIdOrderByVersionDesc(Long departmentId);
}
