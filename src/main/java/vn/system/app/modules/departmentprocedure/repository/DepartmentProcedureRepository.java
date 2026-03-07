package vn.system.app.modules.departmentprocedure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.departmentprocedure.domain.DepartmentProcedure;

@Repository
public interface DepartmentProcedureRepository extends
        JpaRepository<DepartmentProcedure, Long>,
        JpaSpecificationExecutor<DepartmentProcedure> {

}