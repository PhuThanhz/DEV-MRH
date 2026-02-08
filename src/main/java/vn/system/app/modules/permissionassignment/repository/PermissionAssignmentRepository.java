package vn.system.app.modules.permissionassignment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.permissionassignment.domain.PermissionAssignment;

@Repository
public interface PermissionAssignmentRepository extends JpaRepository<PermissionAssignment, Long> {

        List<PermissionAssignment> findByPermissionContent_Id(Long contentId);

        void deleteByPermissionContent_IdAndDepartmentJobTitle_Id(Long contentId, Long djtId);
}
