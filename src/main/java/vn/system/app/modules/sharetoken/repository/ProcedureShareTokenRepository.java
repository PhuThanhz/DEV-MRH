package vn.system.app.modules.sharetoken.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import vn.system.app.modules.sharetoken.domain.ProcedureShareToken;

@Repository
public interface ProcedureShareTokenRepository extends JpaRepository<ProcedureShareToken, Long> {

    Optional<ProcedureShareToken> findByToken(String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProcedureShareToken p WHERE p.token = :token")
    Optional<ProcedureShareToken> findByTokenWithLock(@Param("token") String token);

    List<ProcedureShareToken> findByProcedureIdAndProcedureType(Long procedureId, String procedureType);
}