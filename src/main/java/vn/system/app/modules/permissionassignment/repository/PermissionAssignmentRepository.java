package vn.system.app.modules.permissionassignment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.permissionassignment.domain.PermissionAssignment;

@Repository
public interface PermissionAssignmentRepository
        extends JpaRepository<PermissionAssignment, Long> {

    // =================================
    // CHECK TRÙNG GÁN QUYỀN
    // =================================
    boolean existsByPermissionContent_IdAndDepartmentJobTitle_Id(
            Long permissionContentId,
            Long departmentJobTitleId);

    // =================================
    // LOAD THEO NỘI DUNG QUYỀN
    // =================================
    List<PermissionAssignment> findByPermissionContent_Id(Long permissionContentId);
}
