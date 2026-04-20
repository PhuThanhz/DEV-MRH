package vn.system.app.modules.confidentialprocedure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.confidentialprocedure.domain.ConfidentialProcedureAccess;

@Repository
public interface ConfidentialProcedureAccessRepository
        extends JpaRepository<ConfidentialProcedureAccess, Long> {

    // Lấy tất cả access của một quy trình
    List<ConfidentialProcedureAccess> findByProcedure_Id(Long procedureId);

    // Kiểm tra tồn tại access theo User
    boolean existsByProcedure_IdAndUserIdAndAccessType(
            Long procedureId, String userId, String accessType);

    // Kiểm tra tồn tại access theo Role
    boolean existsByProcedure_IdAndRoleIdAndAccessType(
            Long procedureId, Long roleId, String accessType);

    // Tìm access theo User (dùng cho Revoke)
    Optional<ConfidentialProcedureAccess> findByProcedure_IdAndUserIdAndAccessType(
            Long procedureId, String userId, String accessType);

    // Xóa access theo User (dùng cho Share lại sau Revoke)
    void deleteByProcedure_IdAndUserIdAndAccessType(
            Long procedureId, String userId, String accessType);

    // Tùy chọn: Xóa tất cả access của một quy trình (nếu cần)
    void deleteByProcedure_Id(Long procedureId);
}