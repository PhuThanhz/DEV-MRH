package vn.system.app.modules.task.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.task.domain.TaskComment;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

    List<TaskComment> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    void deleteByTaskId(Long taskId);
}
