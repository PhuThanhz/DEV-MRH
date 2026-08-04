package vn.system.app.modules.task.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.task.domain.TaskSubmission;

@Repository
public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {

    List<TaskSubmission> findByTaskIdOrderBySubmissionRoundAsc(Long taskId);

    Optional<TaskSubmission> findTopByTaskIdOrderBySubmissionRoundDesc(Long taskId);

    List<TaskSubmission> findByTaskIdInOrderByTaskIdAscSubmissionRoundDesc(List<Long> taskIds);

    Optional<TaskSubmission> findByTaskIdAndSubmissionRound(Long taskId, Integer submissionRound);

    void deleteByTaskId(Long taskId);
}
