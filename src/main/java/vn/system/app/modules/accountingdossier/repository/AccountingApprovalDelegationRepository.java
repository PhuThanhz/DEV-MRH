package vn.system.app.modules.accountingdossier.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.accountingdossier.domain.AccountingApprovalDelegation;
import vn.system.app.modules.accountingdossier.domain.enums.DelegationStatus;

@Repository
public interface AccountingApprovalDelegationRepository extends JpaRepository<AccountingApprovalDelegation, Long> {

    @Query("select d from AccountingApprovalDelegation d, User delegator, User delegate " +
           "where delegator.id = d.delegatorUserId and delegate.id = d.delegateUserId " +
           "and (:admin = true or d.delegatorUserId = :userId or d.delegateUserId = :userId or (:companyLevel = true and d.companyId in :companyIds)) " +
           "and (:keyword is null or lower(coalesce(d.reason, '')) like lower(concat('%', :keyword, '%')) " +
           "or lower(delegator.name) like lower(concat('%', :keyword, '%')) or lower(delegator.email) like lower(concat('%', :keyword, '%')) " +
           "or lower(delegate.name) like lower(concat('%', :keyword, '%')) or lower(delegate.email) like lower(concat('%', :keyword, '%'))) " +
           "and (:status is null or (:status = 'EXPIRED' and d.validTo < :now) or (:status = 'UPCOMING' and d.status = vn.system.app.modules.accountingdossier.domain.enums.DelegationStatus.ACTIVE and d.validFrom > :now) or (:status not in ('EXPIRED', 'UPCOMING') and cast(d.status as string) = :status))")
    Page<AccountingApprovalDelegation> findVisible(@Param("admin") boolean admin, @Param("companyLevel") boolean companyLevel, @Param("userId") String userId,
            @Param("companyIds") Collection<Long> companyIds, @Param("keyword") String keyword,
            @Param("status") String status, @Param("now") Instant now, Pageable pageable);

    List<AccountingApprovalDelegation> findByDelegatorUserIdAndDelegateUserIdAndStatusAndValidFromLessThanEqualAndValidToGreaterThanEqual(
            String delegatorUserId,
            String delegateUserId,
            DelegationStatus status,
            Instant validFrom,
            Instant validTo);

    List<AccountingApprovalDelegation> findByStatusAndValidToBefore(DelegationStatus status, Instant now);

    boolean existsByDelegatorUserIdAndDelegateUserIdAndStatusInAndValidFromLessThanEqualAndValidToGreaterThanEqual(
            String delegatorUserId,
            String delegateUserId,
            Collection<DelegationStatus> statuses,
            Instant validTo,
            Instant validFrom);
}
