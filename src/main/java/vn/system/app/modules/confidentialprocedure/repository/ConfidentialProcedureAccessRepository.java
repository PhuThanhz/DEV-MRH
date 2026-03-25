package vn.system.app.modules.confidentialprocedure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.confidentialprocedure.domain.ConfidentialProcedureAccess;

@Repository
public interface ConfidentialProcedureAccessRepository
        extends JpaRepository<ConfidentialProcedureAccess, Long> {

    List<ConfidentialProcedureAccess> findByProcedure_Id(Long procedureId);

    boolean existsByProcedure_IdAndUserIdAndAccessType(Long procedureId, Long userId, String accessType);

    boolean existsByProcedure_IdAndRoleIdAndAccessType(Long procedureId, Long roleId, String accessType);
}