package vn.system.app.modules.permissioncontent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.permissioncontent.domain.PermissionContent;

@Repository
public interface PermissionContentRepository
                extends JpaRepository<PermissionContent, Long>,
                JpaSpecificationExecutor<PermissionContent> {

        // ===============================
        // LẤY NỘI DUNG THEO DANH MỤC
        // ===============================
        List<PermissionContent> findByCategory_Id(Long categoryId);

        // ===============================
        // CHECK TRÙNG TÊN TRONG DANH MỤC
        // (rất nên có)
        // ===============================
        boolean existsByNameAndCategory_Id(String name, Long categoryId);
}
