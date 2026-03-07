package vn.system.app.modules.department.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.department.domain.Department;

@Repository
public interface DepartmentRepository
        extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    // kiểm tra trùng mã phòng ban
    boolean existsByCode(String code);

    // lấy phòng ban theo công ty
    List<Department> findByCompanyId(Long companyId);
}