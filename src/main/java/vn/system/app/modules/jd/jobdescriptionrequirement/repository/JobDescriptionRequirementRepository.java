package vn.system.app.modules.jd.jobdescriptionrequirement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jd.jobdescriptionrequirement.domain.JobDescriptionRequirement;

@Repository
public interface JobDescriptionRequirementRepository
        extends JpaRepository<JobDescriptionRequirement, Long> {

    Optional<JobDescriptionRequirement> findByJobDescription_Id(Long jobDescriptionId);

}