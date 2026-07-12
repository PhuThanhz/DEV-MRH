package vn.system.app.modules.accountingdossier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierOutbox;
import java.time.Instant;
import java.util.List;

@Repository
public interface AccountingDossierOutboxRepository extends JpaRepository<AccountingDossierOutbox, Long> {

    @Query("SELECT o FROM AccountingDossierOutbox o " +
           "WHERE (o.status = 'PENDING' OR o.status = 'FAILED') " +
           "AND o.retryCount < 3 " +
           "AND o.nextRetryAt <= :now " +
           "ORDER BY o.createdAt ASC")
    List<AccountingDossierOutbox> findPendingToProcess(@Param("now") Instant now, Pageable pageable);
    
    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("select o.idempotencyKey from AccountingDossierOutbox o where o.idempotencyKey in :keys")
    List<String> findExistingIdempotencyKeys(@Param("keys") java.util.Collection<String> keys);
}
