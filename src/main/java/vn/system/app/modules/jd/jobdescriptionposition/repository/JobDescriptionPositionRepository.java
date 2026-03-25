package vn.system.app.modules.jd.jobdescriptionposition.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jd.jobdescriptionposition.domain.JobDescriptionPosition;

@Repository
public interface JobDescriptionPositionRepository
        extends JpaRepository<JobDescriptionPosition, Long> {

    List<JobDescriptionPosition> findByJobDescription_Id(Long jobDescriptionId);

}