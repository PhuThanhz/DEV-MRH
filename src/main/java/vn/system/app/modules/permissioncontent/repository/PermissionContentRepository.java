package vn.system.app.modules.permissioncontent.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.permissioncontent.domain.PermissionContent;

@Repository
public interface PermissionContentRepository
                extends JpaRepository<PermissionContent, Long> {

        /*
         * =====================
         * FETCH BY CATEGORY
         * =====================
         */
        Page<PermissionContent> findByCategory_Id(Long categoryId, Pageable pageable);

        /*
         * =====================
         * ACTIVE ONLY
         * =====================
         */
        PermissionContent findByIdAndActiveTrue(Long id);

        boolean existsByNameAndCategory_IdAndActiveTrue(
                        String name,
                        Long categoryId);

        List<PermissionContent> findByCategory_IdAndActiveTrue(Long categoryId);
}
