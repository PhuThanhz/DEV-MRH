package vn.system.app.modules.evaluation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.system.app.modules.evaluation.domain.EvaluationHistory;
import vn.system.app.modules.evaluation.domain.EvaluationPeriod;
import vn.system.app.modules.evaluation.domain.EvaluationRecord;
import vn.system.app.modules.evaluation.domain.enums.PeriodStatus;
import vn.system.app.modules.evaluation.domain.enums.RecordStatus;
import vn.system.app.modules.evaluation.repository.EvaluationHistoryRepository;
import vn.system.app.modules.evaluation.repository.EvaluationPeriodRepository;
import vn.system.app.modules.evaluation.repository.EvaluationRecordRepository;
import vn.system.app.modules.notification.repository.NotificationRepository;
import vn.system.app.modules.notification.service.NotificationService;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.userposition.repository.UserPositionRepository;
import vn.system.app.modules.adminscope.service.UserAdminScopeService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationReminderScheduler {

    private final EvaluationPeriodRepository periodRepo;
    private final EvaluationRecordRepository recordRepo;
    private final NotificationRepository notificationRepo;
    private final EvaluationHistoryRepository historyRepo;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepo;
    private final UserPositionRepository userPositionRepo;
    private final UserAdminScopeService userAdminScopeService;
    // T12: cần gọi systemAutoAcknowledge
    private final EvaluationRecordService recordService;

    // Chạy mỗi ngày lúc 00:00 (midnight) theo giờ Việt Nam
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Ho_Chi_Minh")
    /**
     * ⚠️ QUAN TRỌNG: Cần giữ nguyên annotation @Transactional ở đây.
     * Lý do: Cơ chế đẩy thông báo in-app và gửi email dùng @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT).
     * Nếu gỡ bỏ @Transactional ở hàm này, Spring sẽ không mở transaction, dẫn đến toàn bộ sự kiện nhắc hạn
     * chót và mở kỳ sẽ BỊ NUỐT hoàn toàn (không có transaction để commit thì AFTER_COMMIT listener không bao giờ chạy).
     */
    @Transactional
    public void sendReminders() {
        log.info("Bắt đầu chạy cron job nhắc nhở đánh giá HQCV...");

        List<EvaluationPeriod> activePeriods = periodRepo.findByStatus(PeriodStatus.ACTIVE);
        ZoneId zoneVN = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate today = LocalDate.now(zoneVN);
        Instant todayMidnight = today.atStartOfDay(zoneVN).toInstant();
        Instant now = Instant.now();

        for (EvaluationPeriod period : activePeriods) {
            // 1. Nhắc nhở nhân viên chưa nộp (còn 3 ngày và 1 ngày)
            if (period.getEmployeeDeadline() != null) {
                LocalDate empDeadline = period.getEmployeeDeadline().atZone(zoneVN).toLocalDate();
                long daysLeft = ChronoUnit.DAYS.between(today, empDeadline);
                if (daysLeft <= 3 && daysLeft >= 0) {
                    List<EvaluationRecord> unsubmitted = recordRepo.findByPeriodIdAndStatus(period.getId(), RecordStatus.EMPLOYEE_DRAFTING);
                    String type = "REMINDER_DEADLINE";
                    String actionLink = "/admin/evaluation/my-records";

                    java.util.List<String> empIdsToSend = unsubmitted.stream()
                            .map(r -> r.getEmployee().getId())
                            .distinct()
                            .filter(empId -> !notificationRepo.existsByRecipientIdAndTypeAndActionLinkAndCreatedAtAfter(
                                    empId, type, actionLink, todayMidnight))
                            .toList();

                    if (!empIdsToSend.isEmpty()) {
                        sendNotifications(empIdsToSend, type,
                                String.format("Chỉ còn %d ngày để nộp bản tự đánh giá HQCV. Vui lòng hoàn thành sớm.", daysLeft),
                                actionLink);
                    }
                }
            }

            // 2. Nhắc nhở quản lý trực tiếp chưa chấm (còn 2 ngày)
            if (period.getManagerDeadline() != null) {
                LocalDate mgrDeadline = period.getManagerDeadline().atZone(zoneVN).toLocalDate();
                long daysLeft = ChronoUnit.DAYS.between(today, mgrDeadline);
                if (daysLeft <= 2 && daysLeft >= 0) {
                    List<EvaluationRecord> pendingManager = recordRepo.findByPeriodIdAndStatus(period.getId(), RecordStatus.PENDING_MANAGER_REVIEW);
                    pendingManager.addAll(recordRepo.findByPeriodIdAndStatus(period.getId(), RecordStatus.MANAGER_REVIEWING));
                    
                    String type = "REMINDER_DEADLINE";
                    String actionLink = "/admin/evaluation/manager/pending";

                    java.util.List<String> managerIdsToSend = pendingManager.stream()
                            .map(EvaluationRecord::getDirectManager)
                            .filter(manager -> manager != null)
                            .map(User::getId)
                            .distinct()
                            .filter(managerId -> !notificationRepo.existsByRecipientIdAndTypeAndActionLinkAndCreatedAtAfter(
                                    managerId, type, actionLink, todayMidnight))
                            .toList();

                    if (!managerIdsToSend.isEmpty()) {
                        sendNotifications(managerIdsToSend, type,
                                "Bạn có nhân viên chưa được chấm điểm. Vui lòng hoàn thành trong vòng 2 ngày tới.",
                                actionLink);
                    }
                }
            }

            // 3. Nhắc nhở quản lý gián tiếp chưa duyệt (còn 2 ngày)
            if (period.getApprovalDeadline() != null) {
                LocalDate appDeadline = period.getApprovalDeadline().atZone(zoneVN).toLocalDate();
                long daysLeft = ChronoUnit.DAYS.between(today, appDeadline);
                if (daysLeft <= 2 && daysLeft >= 0) {
                    List<EvaluationRecord> pendingApproval = recordRepo.findByPeriodIdAndStatus(period.getId(), RecordStatus.PENDING_APPROVAL);
                    
                    String type = "REMINDER_DEADLINE";
                    String actionLink = "/admin/evaluation/approval/pending";

                    java.util.List<String> managerIdsToSend = pendingApproval.stream()
                            .map(EvaluationRecord::getIndirectManager)
                            .filter(manager -> manager != null)
                            .map(User::getId)
                            .distinct()
                            .filter(managerId -> !notificationRepo.existsByRecipientIdAndTypeAndActionLinkAndCreatedAtAfter(
                                    managerId, type, actionLink, todayMidnight))
                            .toList();

                    if (!managerIdsToSend.isEmpty()) {
                        sendNotifications(managerIdsToSend, type,
                                "Bạn có bản đánh giá cần phê duyệt. Vui lòng hoàn thành trong vòng 2 ngày tới.",
                                actionLink);
                    }
                }
            }

            // 4. Leo thang trễ hạn (Escalation)
            List<EvaluationRecord> allRecords = recordRepo.findByPeriodId(period.getId());
            for (EvaluationRecord record : allRecords) {
                RecordStatus status = record.getStatus();
                if (status == RecordStatus.COMPLETED || status == RecordStatus.CANCELLED || status == RecordStatus.NOT_STARTED) {
                    continue;
                }

                Instant deadline = null;
                if (status == RecordStatus.EMPLOYEE_DRAFTING) {
                    deadline = record.getEmployeeDeadlineOverride() != null ? record.getEmployeeDeadlineOverride() : period.getEmployeeDeadline();
                } else if (status == RecordStatus.PENDING_MANAGER_REVIEW || status == RecordStatus.MANAGER_REVIEWING || status == RecordStatus.REVISION_NEEDED) {
                    deadline = record.getManagerDeadlineOverride() != null ? record.getManagerDeadlineOverride() : period.getManagerDeadline();
                } else if (status == RecordStatus.PENDING_APPROVAL) {
                    deadline = record.getApprovalDeadlineOverride() != null ? record.getApprovalDeadlineOverride() : period.getApprovalDeadline();
                }

                if (deadline != null && now.isAfter(deadline)) {
                    List<String> adminIds = getAdminRecipientIdsForEmployee(record.getEmployee());
                    if (!adminIds.isEmpty()) {
                        String actionLink = "/admin/evaluation/periods/" + period.getId() + "/progress";
                        List<String> adminsToSend = adminIds.stream()
                                .filter(adminId -> !notificationRepo.existsByRecipientIdAndTypeAndActionLinkAndCreatedAtAfter(
                                        adminId, "ESCALATION", actionLink, todayMidnight))
                                .toList();

                        if (!adminsToSend.isEmpty()) {
                            String empName = record.getEmployee() != null ? record.getEmployee().getName() : "Nhân viên";
                            String statusLabel = getStatusLabel(status);
                            sendNotifications(adminsToSend, "ESCALATION",
                                    String.format("Bản đánh giá HQCV của nhân viên %s đang quá hạn ở bước %s.", empName, statusLabel),
                                    actionLink);
                        }
                    }
                }
            }
        }

        // ─── T12: Nhắc nhân viên xác nhận đã xem (sau 1 ngày, nhắc lại mỗi 3 ngày, tối đa 2 lần) ───
        // Lấy record COMPLETED, completedAt = null, approvedAt cách đây > 1 ngày
        Instant oneDayAgo = now.minus(1, ChronoUnit.DAYS);
        List<EvaluationRecord> unacknowledged = recordRepo.findCompletedNotAcknowledgedApprovedBefore(oneDayAgo);

        String remindType = "REMIND_ACKNOWLEDGE";
        String remindLink = "/admin/evaluation/my-records";

        for (EvaluationRecord rec : unacknowledged) {
            long daysSinceApproved = ChronoUnit.DAYS.between(
                    rec.getApprovedAt().atZone(zoneVN).toLocalDate(), today);

            // ─── Auto-acknowledge sau 7 ngày ───────────────────────────────────
            if (daysSinceApproved >= 7) {
                // Gọi service trong context transaction hiện tại
                try {
                    recordService.systemAutoAcknowledge(rec.getId());
                    log.info("[T12] Auto-acknowledged record ID {} sau {} ngày", rec.getId(), daysSinceApproved);
                } catch (Exception e) {
                    log.warn("[T12] Lỗi auto-acknowledge record ID {}: {}", rec.getId(), e.getMessage());
                }
                continue;
            }

            // ─── Nhắc lại sau 3 ngày và 6 ngày (2 lần nhắc trong 7 ngày) ─────
            boolean shouldRemind = (daysSinceApproved == 3 || daysSinceApproved == 6);
            if (!shouldRemind) continue;

            String empId = rec.getEmployee().getId();
            // Idempotent: chỉ gửi 1 lần/ngày
            boolean alreadySent = notificationRepo.existsByRecipientIdAndTypeAndActionLinkAndCreatedAtAfter(
                    empId, remindType, remindLink, todayMidnight);
            if (!alreadySent) {
                sendNotifications(List.of(empId), remindType,
                        "Bạn chưa xác nhận đã xem kết quả đánh giá HQCV. Vui lòng vào hệ thống để xác nhận.",
                        remindLink);
                log.info("[T12] Đã nhắc nhân viên {} xác nhận sau {} ngày", empId, daysSinceApproved);
            }
        }

        log.info("Đã hoàn thành cron job nhắc nhở đánh giá HQCV.");
    }

    private List<String> getAdminRecipientIdsForEmployee(User employee) {
        List<Long> empCompanyIds = userPositionRepo.findActiveCompanyIdsByUserId(employee.getId());
        List<Long> empDeptIds = userPositionRepo.findActiveDepartmentIdsByUserId(employee.getId());

        List<User> activeAdmins = userRepo.findActiveUsersByRoleNames(List.of(
                "SUPER_ADMIN", "ADMIN_SUB_1", "ADMIN_SUB_2", "ADMIN_SUB_3"
        ));

        List<String> recipientIds = new java.util.ArrayList<>();
        for (User admin : activeAdmins) {
            String roleName = admin.getRole() != null ? admin.getRole().getName() : "";
            if ("SUPER_ADMIN".equals(roleName) || "ADMIN_SUB_1".equals(roleName)) {
                recipientIds.add(admin.getId());
            } else if ("ADMIN_SUB_2".equals(roleName)) {
                Set<Long> adminCompIds = userAdminScopeService.getCompanyScopeIds(admin.getId());
                if (empCompanyIds.stream().anyMatch(adminCompIds::contains)) {
                    recipientIds.add(admin.getId());
                }
            } else if ("ADMIN_SUB_3".equals(roleName)) {
                Set<Long> adminDeptIds = userAdminScopeService.getDepartmentScopeIds(admin.getId(), roleName);
                if (empDeptIds.stream().anyMatch(adminDeptIds::contains)) {
                    recipientIds.add(admin.getId());
                }
            }
        }
        return recipientIds.stream().distinct().toList();
    }

    private String getStatusLabel(RecordStatus status) {
        if (status == null) return "Không xác định";
        return switch (status) {
            case EMPLOYEE_DRAFTING -> "Nhân viên tự đánh giá";
            case PENDING_MANAGER_REVIEW, MANAGER_REVIEWING -> "Quản lý trực tiếp đánh giá";
            case REVISION_NEEDED -> "Nhân viên chỉnh sửa";
            case PENDING_APPROVAL -> "Người phê duyệt đánh giá";
            default -> status.toString();
        };
    }

    private void sendNotifications(java.util.Collection<String> userIds, String type, String content, String actionLink) {
        eventPublisher.publishEvent(new vn.system.app.modules.notification.event.AppNotificationEvent(
                new java.util.ArrayList<>(userIds), "EVALUATION", type, content, actionLink));
    }

    // Tự động quét mở cổng tự đánh giá mỗi 60 giây (1 phút)
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void autoOpenPeriods() {
        List<EvaluationPeriod> activePeriods = periodRepo.findByStatus(PeriodStatus.ACTIVE);
        Instant now = Instant.now();

        for (EvaluationPeriod period : activePeriods) {
            if (period.getEmployeeStartDate() != null && !now.isBefore(period.getEmployeeStartDate())) {
                List<EvaluationRecord> notStartedRecords = recordRepo.findByPeriodIdAndStatus(period.getId(), RecordStatus.NOT_STARTED);
                java.util.List<String> employeeIds = new java.util.ArrayList<>();
                for (EvaluationRecord record : notStartedRecords) {
                    record.setStatus(RecordStatus.EMPLOYEE_DRAFTING);
                    
                    // Ghi lịch sử Audit
                    EvaluationHistory history = new EvaluationHistory();
                    history.setEvaluationRecord(record);
                    history.setFromStatus(RecordStatus.NOT_STARTED);
                    history.setToStatus(RecordStatus.EMPLOYEE_DRAFTING);
                    history.setPerformedBy(null); // Hệ thống tự động
                    history.setNote("Hệ thống tự động mở cổng tự đánh giá khi đến ngày");
                    historyRepo.save(history);

                    employeeIds.add(record.getEmployee().getId());
                }
                if (!notStartedRecords.isEmpty()) {
                    recordRepo.saveAll(notStartedRecords);
                }
                if (!employeeIds.isEmpty()) {
                    sendNotifications(employeeIds, "PERIOD_OPENED",
                            String.format("Kỳ đánh giá \"%s\" đã mở. Vui lòng hoàn thành tự đánh giá trước hạn chót.", period.getName()),
                            "/admin/evaluation/my-records");
                }
            }
        }
    }
}
