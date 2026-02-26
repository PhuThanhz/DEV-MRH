package vn.system.app.modules.jd.duty.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jd.duty.domain.JDDuty;

@Repository
public interface JDDutyRepository
        extends JpaRepository<JDDuty, Long> {

    Optional<JDDuty> findByJobDescriptionIdAndActiveTrue(Long jobDescriptionId);
}