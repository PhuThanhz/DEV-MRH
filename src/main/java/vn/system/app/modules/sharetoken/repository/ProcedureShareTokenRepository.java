package vn.system.app.modules.sharetoken.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.sharetoken.domain.ProcedureShareToken;

@Repository
public interface ProcedureShareTokenRepository extends JpaRepository<ProcedureShareToken, Long> {

    Optional<ProcedureShareToken> findByToken(String token);

    List<ProcedureShareToken> findByProcedureIdAndProcedureType(Long procedureId, String procedureType);
}