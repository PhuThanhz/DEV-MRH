package vn.system.app.modules.evaluation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.system.app.modules.evaluation.domain.EvaluationPeriod;
import vn.system.app.modules.evaluation.domain.EvaluationRecord;
import vn.system.app.modules.evaluation.domain.enums.NotificationType;
import vn.system.app.modules.evaluation.domain.enums.PeriodStatus;
import vn.system.app.modules.evaluation.domain.enums.RecordStatus;
import vn.system.app.modules.evaluation.repository.EvaluationPeriodRepository;
import vn.system.app.modules.evaluation.repository.EvaluationRecordRepository;
import vn.system.app.modules.evaluation.domain.EvaluationNotification;
import vn.system.app.modules.evaluation.repository.EvaluationNotificationRepository;
import vn.system.app.modules.user.repository.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationReminderScheduler {

    private final EvaluationPeriodRepository periodRepo;
    private final EvaluationRecordRepository recordRepo;
    private final EvaluationNotificationRepository notificationRepo;
    private final UserRepository userRepo;

    // Chạy mỗi ngày lúc 00:00 (midnight)
    @Scheduled(cron = "0 0 0 * * ?")
    public void sendReminders() {
        log.info("Bắt đầu chạy cron job nhắc nhở đánh giá HQCV...");

        List<EvaluationPeriod> activePeriods = periodRepo.findByStatus(PeriodStatus.ACTIVE);
        Instant now = Instant.now();

        for (EvaluationPeriod period : activePeriods) {
            // 1. Nhắc nhở nhân viên chưa nộp (còn 3 ngày và 1 ngày)
            if (period.getEmployeeDeadline() != null) {
                long daysLeft = ChronoUnit.DAYS.between(now, period.getEmployeeDeadline());
                if (daysLeft == 3 || daysLeft == 1) {
                    List<EvaluationRecord> unsubmitted = recordRepo.findByPeriodIdAndStatus(period.getId(), RecordStatus.EMPLOYEE_DRAFTING);
                    for (EvaluationRecord record : unsubmitted) {
                        sendNotification(record.getEmployee().getId(), NotificationType.REMINDER_DEADLINE,
                                String.format("Chỉ còn %d ngày để nộp bản tự đánh giá HQCV. Vui lòng hoàn thành sớm.", daysLeft),
                                "/evaluation/my-records/" + record.getId());
                    }
                }
            }

            // 2. Nhắc nhở quản lý trực tiếp chưa chấm (còn 2 ngày)
            if (period.getManagerDeadline() != null) {
                long daysLeft = ChronoUnit.DAYS.between(now, period.getManagerDeadline());
                if (daysLeft == 2) {
                    List<EvaluationRecord> pendingManager = recordRepo.findByPeriodIdAndStatus(period.getId(), RecordStatus.PENDING_MANAGER_REVIEW);
                    pendingManager.addAll(recordRepo.findByPeriodIdAndStatus(period.getId(), RecordStatus.MANAGER_REVIEWING));
                    
                    // Group by manager to avoid spamming
                    pendingManager.stream().map(r -> r.getDirectManager().getId()).distinct().forEach(managerId -> {
                        sendNotification(managerId, NotificationType.REMINDER_DEADLINE,
                                "Bạn có nhân viên chưa được chấm điểm. Vui lòng hoàn thành trong vòng 2 ngày tới.",
                                "/evaluation/manager/dashboard");
                    });
                }
            }

            // 3. Nhắc nhở quản lý gián tiếp chưa duyệt (còn 2 ngày)
            if (period.getApprovalDeadline() != null) {
                long daysLeft = ChronoUnit.DAYS.between(now, period.getApprovalDeadline());
                if (daysLeft == 2) {
                    List<EvaluationRecord> pendingApproval = recordRepo.findByPeriodIdAndStatus(period.getId(), RecordStatus.PENDING_APPROVAL);
                    
                    pendingApproval.stream().map(r -> r.getIndirectManager().getId()).distinct().forEach(managerId -> {
                        sendNotification(managerId, NotificationType.REMINDER_DEADLINE,
                                "Bạn có bản đánh giá cần phê duyệt. Vui lòng hoàn thành trong vòng 2 ngày tới.",
                                "/evaluation/approval/dashboard");
                    });
                }
            }
        }
        
        log.info("Đã hoàn thành cron job nhắc nhở đánh giá HQCV.");
    }

    private void sendNotification(String userId, NotificationType type, String content, String actionLink) {
        userRepo.findById(userId).ifPresent(user -> {
            EvaluationNotification notification = new EvaluationNotification();
            notification.setRecipient(user);
            notification.setNotificationType(type);
            notification.setContent(content);
            notification.setActionLink(actionLink);
            notificationRepo.save(notification);
        });
    }
}
