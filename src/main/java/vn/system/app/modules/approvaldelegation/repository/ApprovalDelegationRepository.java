package vn.system.app.modules.approvaldelegation.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.approvaldelegation.domain.ApprovalDelegation;
import vn.system.app.modules.approvaldelegation.domain.enums.ApprovalDelegationStatus;

@Repository
public interface ApprovalDelegationRepository
        extends JpaRepository<ApprovalDelegation, Long>, JpaSpecificationExecutor<ApprovalDelegation> {

    List<ApprovalDelegation> findByDelegatorUserId(String delegatorUserId);

    List<ApprovalDelegation> findByDelegatorUserIdAndModuleAndStatus(String delegatorUserId, String module, ApprovalDelegationStatus status);

    List<ApprovalDelegation> findByDelegateUserId(String delegateUserId);

    List<ApprovalDelegation> findByStatusAndValidToBefore(ApprovalDelegationStatus status, Instant now);

    @Query("""
        SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
        FROM ApprovalDelegation d
        WHERE d.module = :module
        AND d.delegatorUser.id = :delegatorUserId
        AND d.delegateUser.id = :delegateUserId
        AND d.status = 'ACTIVE'
        AND d.validFrom <= :now
        AND d.validTo >= :now
    """)
    boolean canActAsDelegate(
            @Param("module") String module,
            @Param("delegatorUserId") String delegatorUserId,
            @Param("delegateUserId") String delegateUserId,
            @Param("now") Instant now);

    @Query("""
        SELECT d FROM ApprovalDelegation d
        WHERE d.module = :module
        AND d.delegatorUser.id = :delegatorUserId
        AND d.status = 'ACTIVE'
        AND d.validFrom <= :now
        AND d.validTo >= :now
    """)
    List<ApprovalDelegation> findActiveDelegationsByDelegator(
            @Param("module") String module,
            @Param("delegatorUserId") String delegatorUserId,
            @Param("now") Instant now);
}
