package vn.system.app.modules.jd.jobdescriptiontask.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jd.jobdescriptiontask.domain.JobDescriptionTaskItem;

@Repository
public interface JobDescriptionTaskItemRepository
        extends JpaRepository<JobDescriptionTaskItem, Long> {

    List<JobDescriptionTaskItem> findByJobDescriptionTask_IdOrderByOrderNo(Long jobDescriptionTaskId);

    List<JobDescriptionTaskItem> findByJobDescriptionTask_IdInOrderByOrderNo(List<Long> jobDescriptionTaskIds);

    void deleteByJobDescriptionTask_Id(Long jobDescriptionTaskId);

}
