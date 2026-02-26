package vn.system.app.modules.jd.requirement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jd.requirement.domain.JDRequirement;

@Repository
public interface JDRequirementRepository
        extends JpaRepository<JDRequirement, Long> {

    Optional<JDRequirement> findByJobDescriptionIdAndActiveTrue(Long jobDescriptionId);
}