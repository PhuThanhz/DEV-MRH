package vn.system.app.modules.departmentobjective.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import vn.system.app.modules.departmentobjective.domain.DepartmentObjective;

public interface DepartmentObjectiveRepository
        extends JpaRepository<DepartmentObjective, Long>,
        JpaSpecificationExecutor<DepartmentObjective> {

    List<DepartmentObjective> findByDepartmentId(Long departmentId);

    void deleteByDepartmentId(Long departmentId);
}