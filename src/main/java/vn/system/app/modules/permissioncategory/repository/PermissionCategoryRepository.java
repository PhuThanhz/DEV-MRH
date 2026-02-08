package vn.system.app.modules.permissioncategory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.permissioncategory.domain.PermissionCategory;

@Repository
public interface PermissionCategoryRepository
        extends JpaRepository<PermissionCategory, Long>,
        JpaSpecificationExecutor<PermissionCategory> {

    /**
     * Kiểm tra trùng code (không phân biệt active)
     * Dùng cho các case cần đảm bảo code unique toàn hệ thống
     */
    boolean existsByCode(String code);

    /**
     * Kiểm tra trùng code trong các category đang active
     * Dùng khi create / update để tránh trùng logic nghiệp vụ
     */
    boolean existsByCodeAndActiveTrue(String code);
}
