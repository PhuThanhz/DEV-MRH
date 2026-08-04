package vn.system.app.modules.task.service;

import java.time.Instant;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.calendar.CalendarEventProvider;
import vn.system.app.common.calendar.UnifiedCalendarEventDTO;
import vn.system.app.modules.task.domain.Task;
import vn.system.app.modules.task.domain.TaskParticipant;
import vn.system.app.modules.task.domain.enums.TaskParticipantRole;
import vn.system.app.modules.task.domain.enums.TaskStatus;
import vn.system.app.modules.task.repository.TaskParticipantRepository;
import vn.system.app.modules.task.repository.TaskRepository;

@Service
public class TaskCalendarEventProvider implements CalendarEventProvider {

    private final TaskRepository taskRepository;
    private final TaskParticipantRepository participantRepository;

    public TaskCalendarEventProvider(TaskRepository taskRepository, TaskParticipantRepository participantRepository) {
        this.taskRepository = taskRepository;
        this.participantRepository = participantRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnifiedCalendarEventDTO> getMyPendingEventsInRange(String userId, Instant from, Instant to) {
        if (userId == null || userId.isBlank()) {
            return List.of();
        }

        // Find all tasks where user is Assignee
        List<TaskParticipant> assigneeParticipants = participantRepository.findByUserIdAndRole(userId, TaskParticipantRole.ASSIGNEE);
        if (assigneeParticipants.isEmpty()) {
            return List.of();
        }

        List<Long> taskIds = assigneeParticipants.stream()
                .map(p -> p.getTask().getId())
                .distinct()
                .toList();

        List<Task> tasks = taskRepository.findAllById(taskIds);

        List<UnifiedCalendarEventDTO> events = new ArrayList<>();
        for (Task t : tasks) {
            if (t.getStatus() == TaskStatus.COMPLETED || t.getStatus() == TaskStatus.CANCELLED) {
                continue;
            }
            if (t.getDueDate() == null) {
                continue;
            }

            if (from != null && t.getDueDate().isBefore(from)) {
                continue;
            }
            if (to != null && t.getDueDate().isAfter(to)) {
                continue;
            }

            UnifiedCalendarEventDTO dto = new UnifiedCalendarEventDTO();
            dto.setId("task-" + t.getId());
            dto.setModule("TASK");
            dto.setLabel("Tác vụ hằng ngày");
            dto.setTitle(t.getTitle());
            dto.setDescription(t.getDescription());
            dto.setEventDate(t.getDueDate());
            dto.setDueDate(t.getDueDate());
            dto.setPriority(t.getPriority() != null ? t.getPriority().name() : "MEDIUM");
            dto.setActionLink("/admin/tasks");
            dto.setColorCode("#1890ff");
            dto.setTagColor("#1890ff");
            dto.setTagLabel("Tác vụ hằng ngày");
            dto.setTargetId(t.getId());
            dto.setTargetUrl("/admin/tasks");

            events.add(dto);
        }

        return events;
    }
}
