package vn.system.app.modules.task.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.task.domain.TaskExtensionRequest;
import vn.system.app.modules.task.domain.enums.TaskExtensionStatus;

@Repository
public interface TaskExtensionRequestRepository extends JpaRepository<TaskExtensionRequest, Long> {

    List<TaskExtensionRequest> findByTaskIdOrderByRequestedAtDesc(Long taskId);

    Optional<TaskExtensionRequest> findByTaskIdAndStatus(Long taskId, TaskExtensionStatus status);

    boolean existsByTaskIdAndStatus(Long taskId, TaskExtensionStatus status);

    void deleteByTaskId(Long taskId);
}
