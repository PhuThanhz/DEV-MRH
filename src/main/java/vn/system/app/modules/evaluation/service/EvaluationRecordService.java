package vn.system.app.modules.evaluation.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.evaluation.domain.*;
import vn.system.app.modules.evaluation.domain.enums.*;
import vn.system.app.modules.evaluation.repository.*;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;

/**
 * Service xử lý luồng đánh giá HQCV (Giai đoạn 1 → 4).
 *
 * Bao gồm:
 * - Nhân viên tự chấm điểm + nộp (Giai đoạn 1)
 * - Quản lý trực tiếp chấm điểm + gửi phê duyệt (Giai đoạn 2)
 * - Quản lý gián tiếp phê duyệt / trả lại (Giai đoạn 3)
 * - Tính điểm, xếp loại
 */
@Service
public class EvaluationRecordService {

    private final EvaluationRecordRepository recordRepo;
    private final EvaluationScoreRepository scoreRepo;
    private final EvaluationCommentRepository commentRepo;
    private final EvaluationTrainingPlanRepository trainingPlanRepo;
    private final EvaluationHistoryRepository historyRepo;
    private final EvaluationNotificationRepository notificationRepo;
    private final TemplateCriteriaRepository criteriaRepo;
    private final UserRepository userRepo;

    public EvaluationRecordService(
            EvaluationRecordRepository recordRepo,
            EvaluationScoreRepository scoreRepo,
            EvaluationCommentRepository commentRepo,
            EvaluationTrainingPlanRepository trainingPlanRepo,
            EvaluationHistoryRepository historyRepo,
            EvaluationNotificationRepository notificationRepo,
            TemplateCriteriaRepository criteriaRepo,
            UserRepository userRepo) {
        this.recordRepo = recordRepo;
        this.scoreRepo = scoreRepo;
        this.commentRepo = commentRepo;
        this.trainingPlanRepo = trainingPlanRepo;
        this.historyRepo = historyRepo;
        this.notificationRepo = notificationRepo;
        this.criteriaRepo = criteriaRepo;
        this.userRepo = userRepo;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FETCH RECORDS
    // ═══════════════════════════════════════════════════════════════════════════

    public EvaluationRecord fetchRecordById(Long id) {
        return recordRepo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Bản đánh giá không tồn tại"));
    }

    /** Danh sách bản đánh giá của nhân viên (lịch sử) */
    public List<EvaluationRecord> fetchRecordsByEmployee(String employeeId) {
        return recordRepo.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    /** Danh sách form cho quản lý trực tiếp trong kỳ */
    public List<EvaluationRecord> fetchRecordsForDirectManager(Long periodId, String managerId) {
        return recordRepo.findByPeriodIdAndDirectManagerId(periodId, managerId);
    }

    /** Danh sách form cho quản lý gián tiếp trong kỳ */
    public List<EvaluationRecord> fetchRecordsForIndirectManager(Long periodId, String managerId) {
        return recordRepo.findByPeriodIdAndIndirectManagerId(periodId, managerId);
    }

    /** Danh sách form chờ quản lý trực tiếp chấm */
    public List<EvaluationRecord> fetchPendingForDirectManager(String managerId) {
        return recordRepo.findByDirectManagerIdAndStatus(managerId, RecordStatus.PENDING_MANAGER_REVIEW);
    }

    /** Danh sách form chờ quản lý gián tiếp phê duyệt */
    public List<EvaluationRecord> fetchPendingForIndirectManager(String managerId) {
        return recordRepo.findByIndirectManagerIdAndStatus(managerId, RecordStatus.PENDING_APPROVAL);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GIAI ĐOẠN 1: NHÂN VIÊN TỰ ĐÁNH GIÁ
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Nhân viên lưu/cập nhật điểm cho một tiêu chí.
     * Có thể gọi nhiều lần (SAVE DRAFT).
     */
    @Transactional
    public EvaluationScore saveEmployeeScore(Long recordId, Long criteriaId, Double score) {
        EvaluationRecord record = fetchRecordById(recordId);

        if (record.getStatus() != RecordStatus.EMPLOYEE_DRAFTING) {
            throw new IdInvalidException("Bạn không thể chấm điểm ở trạng thái hiện tại");
        }

        validateScore(score);

        // Kiểm tra tiêu chí có sub không → nếu có thì không cho nhập tay
        TemplateCriteria criteria = criteriaRepo.findById(criteriaId)
                .orElseThrow(() -> new IdInvalidException("Tiêu chí không tồn tại"));

        List<TemplateCriteria> subs = criteriaRepo.findByParentCriteriaId(criteriaId);
        if (!subs.isEmpty()) {
            throw new IdInvalidException("Tiêu chí cha có sub-tiêu chí, điểm được tính tự động");
        }

        return saveOrUpdateScore(record, criteria, ScoredBy.EMPLOYEE, score);
    }

    /**
     * Nhân viên nộp tự đánh giá.
     * Validate tất cả tiêu chí đã có điểm.
     * Chuyển trạng thái: EMPLOYEE_DRAFTING → PENDING_MANAGER_REVIEW.
     */
    @Transactional
    public EvaluationRecord submitEmployeeEvaluation(Long recordId) {
        EvaluationRecord record = fetchRecordById(recordId);

        if (record.getStatus() != RecordStatus.EMPLOYEE_DRAFTING) {
            throw new IdInvalidException("Bạn không thể nộp đánh giá ở trạng thái hiện tại");
        }

        // Validate tất cả tiêu chí lá đã có điểm
        validateAllLeafCriteriaScored(record, ScoredBy.EMPLOYEE);

        // Tính điểm tiêu chí cha từ sub
        recalculateParentScores(record, ScoredBy.EMPLOYEE);

        // Tính tổng điểm nhân viên
        double totalScore = calculateTotalScore(record, ScoredBy.EMPLOYEE);
        record.setEmployeeTotalScore(totalScore);

        // Chuyển trạng thái
        RecordStatus oldStatus = record.getStatus();
        record.setStatus(RecordStatus.PENDING_MANAGER_REVIEW);
        record.setEmployeeSubmittedAt(Instant.now());
        recordRepo.save(record);

        // Ghi lịch sử
        saveHistory(record, oldStatus, RecordStatus.PENDING_MANAGER_REVIEW, record.getEmployee(), null);

        // Thông báo quản lý trực tiếp
        sendNotification(record.getDirectManager(), NotificationType.MANAGER_REVIEW_NEEDED,
                String.format("Nhân viên %s đã nộp tự đánh giá. Vui lòng chấm điểm.",
                        record.getEmployee().getName()),
                "/evaluation/manager/records/" + record.getId());

        return record;
    }

    /** Nhân viên lưu nhận xét tự đánh giá (không bắt buộc) */
    @Transactional
    public EvaluationComment saveEmployeeSelfReview(Long recordId, String content, User employee) {
        EvaluationRecord record = fetchRecordById(recordId);

        if (record.getStatus() != RecordStatus.EMPLOYEE_DRAFTING) {
            throw new IdInvalidException("Bạn không thể nhận xét ở trạng thái hiện tại");
        }

        // Tìm comment cũ hoặc tạo mới
        List<EvaluationComment> existing = commentRepo.findByEvaluationRecordIdAndCommentType(
                recordId, CommentType.SELF_REVIEW);

        EvaluationComment comment;
        if (!existing.isEmpty()) {
            comment = existing.get(0);
            comment.setContent(content);
        } else {
            comment = new EvaluationComment();
            comment.setEvaluationRecord(record);
            comment.setCommentType(CommentType.SELF_REVIEW);
            comment.setContent(content);
            comment.setWrittenBy(employee);
        }

        return commentRepo.save(comment);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GIAI ĐOẠN 2: QUẢN LÝ TRỰC TIẾP CHẤM ĐIỂM
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Quản lý trực tiếp lưu điểm cho một tiêu chí.
     * Lần đầu lưu sẽ chuyển status sang MANAGER_REVIEWING.
     */
    @Transactional
    public EvaluationScore saveManagerScore(Long recordId, Long criteriaId, Double score) {
        EvaluationRecord record = fetchRecordById(recordId);

        if (record.getStatus() != RecordStatus.PENDING_MANAGER_REVIEW
                && record.getStatus() != RecordStatus.MANAGER_REVIEWING
                && record.getStatus() != RecordStatus.REVISION_NEEDED) {
            throw new IdInvalidException("Quản lý không thể chấm điểm ở trạng thái hiện tại");
        }

        validateScore(score);

        TemplateCriteria criteria = criteriaRepo.findById(criteriaId)
                .orElseThrow(() -> new IdInvalidException("Tiêu chí không tồn tại"));

        List<TemplateCriteria> subs = criteriaRepo.findByParentCriteriaId(criteriaId);
        if (!subs.isEmpty()) {
            throw new IdInvalidException("Tiêu chí cha có sub-tiêu chí, điểm được tính tự động");
        }

        // Chuyển trạng thái sang MANAGER_REVIEWING nếu đang PENDING hoặc REVISION_NEEDED
        if (record.getStatus() == RecordStatus.PENDING_MANAGER_REVIEW
                || record.getStatus() == RecordStatus.REVISION_NEEDED) {
            RecordStatus oldStatus = record.getStatus();
            record.setStatus(RecordStatus.MANAGER_REVIEWING);
            recordRepo.save(record);
            saveHistory(record, oldStatus, RecordStatus.MANAGER_REVIEWING, record.getDirectManager(), null);
        }

        return saveOrUpdateScore(record, criteria, ScoredBy.MANAGER, score);
    }

    /**
     * Quản lý trực tiếp gửi phê duyệt.
     * Validate đầy đủ điểm.
     * Chuyển: MANAGER_REVIEWING → PENDING_APPROVAL.
     */
    @Transactional
    public EvaluationRecord submitManagerReview(Long recordId) {
        EvaluationRecord record = fetchRecordById(recordId);

        if (record.getStatus() != RecordStatus.MANAGER_REVIEWING) {
            throw new IdInvalidException("Bạn không thể gửi phê duyệt ở trạng thái hiện tại");
        }

        validateAllLeafCriteriaScored(record, ScoredBy.MANAGER);
        recalculateParentScores(record, ScoredBy.MANAGER);

        double totalScore = calculateTotalScore(record, ScoredBy.MANAGER);
        record.setManagerTotalScore(totalScore);
        record.setFinalGrade(calculateGrade(totalScore));

        RecordStatus oldStatus = record.getStatus();
        record.setStatus(RecordStatus.PENDING_APPROVAL);
        record.setManagerSubmittedAt(Instant.now());
        recordRepo.save(record);

        saveHistory(record, oldStatus, RecordStatus.PENDING_APPROVAL, record.getDirectManager(), null);

        // Thông báo quản lý gián tiếp
        sendNotification(record.getIndirectManager(), NotificationType.APPROVAL_NEEDED,
                String.format("Đánh giá nhân viên %s đã được chấm điểm. Vui lòng phê duyệt.",
                        record.getEmployee().getName()),
                "/evaluation/approval/records/" + record.getId());

        return record;
    }

    /** Quản lý trực tiếp lưu nhận xét */
    @Transactional
    public EvaluationComment saveManagerFeedback(Long recordId, String content, User manager) {
        EvaluationRecord record = fetchRecordById(recordId);

        List<EvaluationComment> existing = commentRepo.findByEvaluationRecordIdAndCommentType(
                recordId, CommentType.MANAGER_FEEDBACK);

        EvaluationComment comment;
        if (!existing.isEmpty()) {
            comment = existing.get(0);
            comment.setContent(content);
        } else {
            comment = new EvaluationComment();
            comment.setEvaluationRecord(record);
            comment.setCommentType(CommentType.MANAGER_FEEDBACK);
            comment.setContent(content);
            comment.setWrittenBy(manager);
        }

        return commentRepo.save(comment);
    }

    /** Quản lý trực tiếp lưu kế hoạch đào tạo */
    @Transactional
    public EvaluationTrainingPlan saveTrainingPlan(Long recordId, EvaluationTrainingPlan plan) {
        EvaluationRecord record = fetchRecordById(recordId);
        plan.setEvaluationRecord(record);
        return trainingPlanRepo.save(plan);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GIAI ĐOẠN 3: QUẢN LÝ GIÁN TIẾP PHÊ DUYỆT
    // ═══════════════════════════════════════════════════════════════════════════

    /** Phê duyệt: PENDING_APPROVAL → COMPLETED */
    @Transactional
    public EvaluationRecord approveRecord(Long recordId, User approver) {
        EvaluationRecord record = fetchRecordById(recordId);

        if (record.getStatus() != RecordStatus.PENDING_APPROVAL) {
            throw new IdInvalidException("Bản đánh giá không ở trạng thái chờ phê duyệt");
        }

        RecordStatus oldStatus = record.getStatus();
        record.setStatus(RecordStatus.COMPLETED);
        record.setApprovedAt(Instant.now());
        record.setCompletedAt(Instant.now());
        recordRepo.save(record);

        saveHistory(record, oldStatus, RecordStatus.COMPLETED, approver, null);

        // Thông báo nhân viên
        sendNotification(record.getEmployee(), NotificationType.RESULT_AVAILABLE,
                "Kết quả đánh giá HQCV đã có. Nhấn để xem chi tiết.",
                "/evaluation/my-records/" + record.getId());

        // Thông báo quản lý trực tiếp
        sendNotification(record.getDirectManager(), NotificationType.RESULT_AVAILABLE,
                String.format("Đánh giá nhân viên %s đã được phê duyệt.",
                        record.getEmployee().getName()),
                "/evaluation/manager/records/" + record.getId());

        return record;
    }

    /**
     * Trả lại: PENDING_APPROVAL → REVISION_NEEDED.
     * Bắt buộc phải có lý do.
     */
    @Transactional
    public EvaluationRecord rejectRecord(Long recordId, String reason, User rejector) {
        EvaluationRecord record = fetchRecordById(recordId);

        if (record.getStatus() != RecordStatus.PENDING_APPROVAL) {
            throw new IdInvalidException("Bản đánh giá không ở trạng thái chờ phê duyệt");
        }

        if (reason == null || reason.isBlank()) {
            throw new IdInvalidException("Vui lòng nhập lý do trả lại");
        }

        RecordStatus oldStatus = record.getStatus();
        record.setStatus(RecordStatus.REVISION_NEEDED);
        recordRepo.save(record);

        saveHistory(record, oldStatus, RecordStatus.REVISION_NEEDED, rejector, reason);

        // Lưu lý do trả lại vào comments
        EvaluationComment comment = new EvaluationComment();
        comment.setEvaluationRecord(record);
        comment.setCommentType(CommentType.REJECTION_REASON);
        comment.setContent(reason);
        comment.setWrittenBy(rejector);
        commentRepo.save(comment);

        // Thông báo quản lý trực tiếp
        sendNotification(record.getDirectManager(), NotificationType.REVISION_NEEDED,
                String.format("Đánh giá nhân viên %s đã bị trả lại. Lý do: %s",
                        record.getEmployee().getName(), reason),
                "/evaluation/manager/records/" + record.getId());

        return record;
    }

    /** Đếm số lần bị trả lại */
    public long countRejections(Long recordId) {
        return historyRepo.countByEvaluationRecordIdAndToStatus(recordId, RecordStatus.REVISION_NEEDED);
    }

    /** Lịch sử thay đổi trạng thái */
    public List<EvaluationHistory> fetchHistory(Long recordId) {
        return historyRepo.findByEvaluationRecordIdOrderByPerformedAtDesc(recordId);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BÁO CÁO / THỐNG KÊ
    // ═══════════════════════════════════════════════════════════════════════════

    /** Đếm theo trạng thái trong kỳ */
    public List<Object[]> getStatusDistribution(Long periodId) {
        return recordRepo.countByPeriodGroupByStatus(periodId);
    }

    /** Đếm theo xếp loại A/B/C/D/E trong kỳ */
    public List<Object[]> getGradeDistribution(Long periodId) {
        return recordRepo.countByPeriodGroupByFinalGrade(periodId);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TÍNH ĐIỂM — CORE LOGIC
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Lưu hoặc cập nhật điểm cho tiêu chí.
     * weightedScore = score × criteria.weight × section.weight
     */
    private EvaluationScore saveOrUpdateScore(EvaluationRecord record, TemplateCriteria criteria,
            ScoredBy scoredBy, Double score) {

        EvaluationScore evalScore = scoreRepo
                .findByEvaluationRecordIdAndCriteriaIdAndScoredBy(record.getId(), criteria.getId(), scoredBy)
                .orElse(new EvaluationScore());

        evalScore.setEvaluationRecord(record);
        evalScore.setCriteria(criteria);
        evalScore.setScoredBy(scoredBy);
        evalScore.setScore(score);

        // Tính weighted score
        double sectionWeight = criteria.getSection().getWeight();
        double criteriaWeight = criteria.getWeight();

        // Nếu là sub-tiêu chí → lấy weight của tiêu chí cha
        if (criteria.getParentCriteria() != null) {
            criteriaWeight = criteria.getParentCriteria().getWeight();
            // Sub-tiêu chí chia đều weight cha (trung bình)
            List<TemplateCriteria> siblings = criteriaRepo.findByParentCriteriaId(
                    criteria.getParentCriteria().getId());
            // weighted score cho sub = score × parentWeight × sectionWeight / numSubs
            evalScore.setWeightedScore(score * criteriaWeight * sectionWeight / siblings.size());
        } else {
            evalScore.setWeightedScore(score * criteriaWeight * sectionWeight);
        }

        return scoreRepo.save(evalScore);
    }

    /**
     * Tính lại điểm tiêu chí cha = trung bình cộng điểm các sub.
     */
    private void recalculateParentScores(EvaluationRecord record, ScoredBy scoredBy) {
        List<EvaluationScore> scores = scoreRepo.findByEvaluationRecordIdAndScoredBy(record.getId(), scoredBy);

        // Tìm tất cả tiêu chí cha (có sub)
        scores.stream()
                .map(s -> s.getCriteria().getParentCriteria())
                .filter(parent -> parent != null)
                .distinct()
                .forEach(parent -> {
                    List<TemplateCriteria> subs = criteriaRepo.findByParentCriteriaId(parent.getId());
                    double avgScore = subs.stream()
                            .map(sub -> scoreRepo.findByEvaluationRecordIdAndCriteriaIdAndScoredBy(
                                    record.getId(), sub.getId(), scoredBy))
                            .filter(opt -> opt.isPresent())
                            .mapToDouble(opt -> opt.get().getScore())
                            .average()
                            .orElse(0.0);

                    saveOrUpdateScore(record, parent, scoredBy, avgScore);
                });
    }

    /**
     * Tính tổng điểm cuối = sum(weightedScore) của tất cả tiêu chí lá.
     * Tiêu chí lá = tiêu chí không có sub.
     */
    private double calculateTotalScore(EvaluationRecord record, ScoredBy scoredBy) {
        List<EvaluationScore> scores = scoreRepo.findByEvaluationRecordIdAndScoredBy(record.getId(), scoredBy);

        return scores.stream()
                .filter(s -> {
                    // Chỉ lấy tiêu chí lá (không có sub-tiêu chí)
                    List<TemplateCriteria> subs = criteriaRepo.findByParentCriteriaId(s.getCriteria().getId());
                    return subs.isEmpty();
                })
                .mapToDouble(EvaluationScore::getWeightedScore)
                .sum();
    }

    /**
     * Xếp loại theo tổng điểm:
     *   < 3.0  → E
     *   3.0–3.5 → D
     *   3.5–4.0 → C
     *   4.0–4.5 → B
     *   > 4.5  → A
     */
    private String calculateGrade(double totalScore) {
        if (totalScore > 4.5) return "A";
        if (totalScore > 4.0) return "B";
        if (totalScore > 3.5) return "C";
        if (totalScore >= 3.0) return "D";
        return "E";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VALIDATION HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private void validateScore(Double score) {
        if (score == null || score < 1 || score > 5) {
            throw new IdInvalidException("Điểm phải từ 1 đến 5");
        }
    }

    /**
     * Validate tất cả tiêu chí lá (không có sub) đã được chấm điểm.
     */
    private void validateAllLeafCriteriaScored(EvaluationRecord record, ScoredBy scoredBy) {
        // Lấy tất cả tiêu chí trong template
        EvaluationTemplate template = record.getTemplate();
        List<TemplateCriteria> allCriteria = template.getSections().stream()
                .flatMap(section -> {
                    List<TemplateCriteria> sectionCriteria =
                            criteriaRepo.findBySectionIdOrderByDisplayOrderAsc(section.getId());
                    return sectionCriteria.stream();
                })
                .collect(java.util.stream.Collectors.toList());

        // Lọc tiêu chí lá
        List<TemplateCriteria> leafCriteria = allCriteria.stream()
                .filter(c -> criteriaRepo.findByParentCriteriaId(c.getId()).isEmpty())
                .collect(java.util.stream.Collectors.toList());

        for (TemplateCriteria criteria : leafCriteria) {
            boolean hasScore = scoreRepo
                    .findByEvaluationRecordIdAndCriteriaIdAndScoredBy(record.getId(), criteria.getId(), scoredBy)
                    .isPresent();

            if (!hasScore) {
                throw new IdInvalidException(
                        String.format("Chưa chấm điểm tiêu chí: %s", criteria.getName()));
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPER: GHI LỊCH SỬ & GỬI THÔNG BÁO
    // ═══════════════════════════════════════════════════════════════════════════

    private void saveHistory(EvaluationRecord record, RecordStatus from, RecordStatus to,
            User performer, String note) {
        EvaluationHistory history = new EvaluationHistory();
        history.setEvaluationRecord(record);
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setPerformedBy(performer);
        history.setNote(note);
        historyRepo.save(history);
    }

    private void sendNotification(User recipient, NotificationType type, String content, String actionLink) {
        EvaluationNotification notification = new EvaluationNotification();
        notification.setRecipient(recipient);
        notification.setNotificationType(type);
        notification.setContent(content);
        notification.setActionLink(actionLink);
        notificationRepo.save(notification);
    }
}
