package vn.system.app.modules.jd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jd.domain.JobDescription;

@Repository
public interface JobDescriptionRepository
        extends JpaRepository<JobDescription, Long>,
        JpaSpecificationExecutor<JobDescription> {
}
