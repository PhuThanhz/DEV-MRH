package vn.system.app.modules.task.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.task.domain.TaskAttachment;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {

    List<TaskAttachment> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    List<TaskAttachment> findByTaskIdAndSubmissionRound(Long taskId, Integer submissionRound);

    Optional<TaskAttachment> findFirstByFileNameAndFolder(String fileName, String folder);

    void deleteByTaskId(Long taskId);
}
