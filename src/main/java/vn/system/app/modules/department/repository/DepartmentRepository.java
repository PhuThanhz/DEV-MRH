package vn.system.app.modules.department.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.department.domain.Department;

@Repository
public interface DepartmentRepository
        extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    // ✅ check trùng code trong cùng công ty
    boolean existsByCodeAndCompany_Id(String code, Long companyId);

    // lấy phòng ban theo công ty
    List<Department> findByCompanyId(Long companyId);

    long countByCompany_IdIn(java.util.Set<Long> companyIds);

    List<Department> findByCompany_IdIn(Collection<Long> companyIds);
}