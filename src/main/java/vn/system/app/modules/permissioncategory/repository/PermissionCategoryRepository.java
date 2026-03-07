package vn.system.app.modules.permissioncategory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.system.app.modules.permissioncategory.domain.PermissionCategory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionCategoryRepository
        extends JpaRepository<PermissionCategory, Long>,
        JpaSpecificationExecutor<PermissionCategory> {

    boolean existsByCode(String code);

    boolean existsByCodeAndActiveTrue(String code);

    @Query("""
                SELECT c FROM PermissionCategory c
                WHERE c.department.id = :departmentId
                  AND c.active = TRUE
            """)
    List<PermissionCategory> findByDepartmentId(@Param("departmentId") Long departmentId);
}