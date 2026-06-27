package vn.system.app.modules.accountingdossier.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.accountingdossier.domain.AccountingDossierSequence;

import java.util.Optional;

@Repository
public interface AccountingDossierSequenceRepository extends JpaRepository<AccountingDossierSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AccountingDossierSequence s WHERE s.company.id = :companyId AND s.year = :year")
    Optional<AccountingDossierSequence> findByCompanyIdAndYearWithLock(@Param("companyId") Long companyId, @Param("year") int year);
}
