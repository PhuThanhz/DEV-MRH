package vn.system.app.modules.permissioncategory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.permissioncategory.domain.PermissionCategory;

@Repository
public interface PermissionCategoryRepository
        extends JpaRepository<PermissionCategory, Long>,
        JpaSpecificationExecutor<PermissionCategory> {

    boolean existsByCode(String code);
}
