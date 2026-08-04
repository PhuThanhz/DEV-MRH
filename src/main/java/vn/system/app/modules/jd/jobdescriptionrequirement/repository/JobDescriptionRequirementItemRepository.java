package vn.system.app.modules.jd.jobdescriptionrequirement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jd.jobdescriptionrequirement.domain.JobDescriptionRequirementItem;

@Repository
public interface JobDescriptionRequirementItemRepository
        extends JpaRepository<JobDescriptionRequirementItem, Long> {

    List<JobDescriptionRequirementItem> findByJobDescriptionRequirement_IdOrderByCategoryAscOrderNoAsc(
            Long jobDescriptionRequirementId);

    void deleteByJobDescriptionRequirement_Id(Long jobDescriptionRequirementId);

}
