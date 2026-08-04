package vn.system.app.modules.task.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.task.domain.TaskParticipant;
import vn.system.app.modules.task.domain.enums.TaskParticipantRole;

@Repository
public interface TaskParticipantRepository extends JpaRepository<TaskParticipant, Long> {

    List<TaskParticipant> findByTaskId(Long taskId);

    @EntityGraph(attributePaths = { "task", "user", "user.userInfo" })
    List<TaskParticipant> findByTaskIdIn(List<Long> taskIds);

    void deleteByTaskId(Long taskId);

    void deleteByTaskIdAndRoleNot(Long taskId, TaskParticipantRole role);

    Optional<TaskParticipant> findByTaskIdAndRole(Long taskId, TaskParticipantRole role);

    List<TaskParticipant> findByUserId(String userId);

    List<TaskParticipant> findByUserIdAndRole(String userId, TaskParticipantRole role);

    boolean existsByTaskIdAndUserId(Long taskId, String userId);

    boolean existsByTaskIdAndUserIdAndRole(Long taskId, String userId, TaskParticipantRole role);
}
