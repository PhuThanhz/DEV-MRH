package vn.system.app.modules.permissioncategoryscope.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.permissioncategoryscope.domain.PermissionCategoryScope;

@Repository
public interface PermissionCategoryScopeRepository
                extends JpaRepository<PermissionCategoryScope, Long> {

        // =================================================
        // LẤY TOÀN BỘ SCOPE THEO DANH MỤC
        // =================================================
        List<PermissionCategoryScope> findByCategory_Id(Long categoryId);

        // =================================================
        // CHECK TRÙNG
        // =================================================
        boolean existsByCategory_IdAndDepartmentJobTitleId(
                        Long categoryId,
                        Long departmentJobTitleId);

        // =================================================
        // LOAD / DELETE THEO DANH SÁCH DJT
        // =================================================
        List<PermissionCategoryScope> findByCategory_IdAndDepartmentJobTitleIdIn(
                        Long categoryId,
                        List<Long> departmentJobTitleIds);

        void deleteByCategory_IdAndDepartmentJobTitleIdIn(
                        Long categoryId,
                        List<Long> departmentJobTitleIds);
}
