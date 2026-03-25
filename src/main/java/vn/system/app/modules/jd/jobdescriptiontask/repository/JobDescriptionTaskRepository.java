package vn.system.app.modules.jd.jobdescriptiontask.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jd.jobdescriptiontask.domain.JobDescriptionTask;

@Repository
public interface JobDescriptionTaskRepository
        extends JpaRepository<JobDescriptionTask, Long> {

    List<JobDescriptionTask> findByJobDescription_IdOrderByOrderNo(Long jdId);

}