package vn.system.app.modules.permissionassignment.repository;

import java.util.List;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.permissionassignment.domain.PermissionAssignment;

@Repository
public interface PermissionAssignmentRepository extends JpaRepository<PermissionAssignment, Long> {

        List<PermissionAssignment> findByPermissionContent_Id(Long contentId);

        List<PermissionAssignment> findByPermissionContent_IdIn(Collection<Long> contentIds);

        void deleteByPermissionContent_IdAndDepartmentJobTitle_Id(Long contentId, Long djtId);
}
