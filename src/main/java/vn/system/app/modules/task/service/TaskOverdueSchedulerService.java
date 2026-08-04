package vn.system.app.modules.task.service;

import java.time.Instant;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.modules.notification.service.NotificationService;
import vn.system.app.modules.task.domain.Task;
import vn.system.app.modules.task.domain.TaskParticipant;
import vn.system.app.modules.task.domain.enums.TaskParticipantRole;
import vn.system.app.modules.task.domain.enums.TaskStatus;
import vn.system.app.modules.task.repository.TaskParticipantRepository;
import vn.system.app.modules.task.repository.TaskRepository;

@Service
public class TaskOverdueSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(TaskOverdueSchedulerService.class);

    private final TaskRepository taskRepository;
    private final TaskParticipantRepository participantRepository;
    private final NotificationService notificationService;

    public TaskOverdueSchedulerService(
            TaskRepository taskRepository,
            TaskParticipantRepository participantRepository,
            NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.participantRepository = participantRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 8 * * *") // Daily at 08:00 AM
    @Transactional
    public void scanAndNotifyOverdueTasks() {
        Instant now = Instant.now();

        // Find all non-completed, non-cancelled tasks with due_date in past
        List<Task> overdueTasks = taskRepository.findAll((root, query, cb) -> cb.and(
                cb.isNotNull(root.get("dueDate")),
                cb.lessThan(root.get("dueDate"), now),
                cb.not(root.get("status").in(TaskStatus.COMPLETED, TaskStatus.CANCELLED))
        ));

        if (overdueTasks.isEmpty()) {
            return;
        }

        List<Long> taskIds = overdueTasks.stream().map(Task::getId).toList();
        List<TaskParticipant> participants = participantRepository.findByTaskIdIn(taskIds);

        // Group overdue task count per Assignee
        Map<String, Integer> assigneeOverdueCounts = new HashMap<>();
        for (TaskParticipant p : participants) {
            if (p.getRole() == TaskParticipantRole.ASSIGNEE && p.getUser() != null) {
                assigneeOverdueCounts.put(p.getUser().getId(), assigneeOverdueCounts.getOrDefault(p.getUser().getId(), 0) + 1);
            }
        }

        // Send digest notification (1 notification per user)
        int notifiedCount = 0;
        for (Map.Entry<String, Integer> entry : assigneeOverdueCounts.entrySet()) {
            String userId = entry.getKey();
            int count = entry.getValue();
            String content = "Bạn có " + count + " tác vụ đã quá hạn chót. Vui lòng kiểm tra và cập nhật tiến độ.";
            try {
                notificationService.sendNotification(userId, "TASK", "TASK_OVERDUE", content, "/tasks");
                notifiedCount++;
            } catch (Exception e) {
                log.error("[TaskOverdueScheduler] Error sending overdue notification to user {}: {}", userId, e.getMessage());
            }
        }

        log.info("[TaskOverdueScheduler] Sent overdue digest notifications to {} users for {} overdue tasks", notifiedCount, overdueTasks.size());
    }
}
