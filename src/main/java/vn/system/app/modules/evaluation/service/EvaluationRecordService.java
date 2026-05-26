package vn.system.app.modules.evaluation.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.UserScopeContext;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.evaluation.domain.*;
import vn.system.app.modules.evaluation.domain.enums.*;
import vn.system.app.modules.evaluation.domain.request.ExtendRecordDeadlineRequest;
import vn.system.app.modules.evaluation.repository.*;
import vn.system.app.modules.notification.service.NotificationService;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.userposition.repository.UserPositionRepository;

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
    private final NotificationService notificationService;
    private final TemplateCriteriaRepository criteriaRepo;
    private final UserRepository userRepo;
    private final UserPositionRepository userPositionRepo;

    public EvaluationRecordService(
            EvaluationRecordRepository recordRepo,
            EvaluationScoreRepository scoreRepo,
            EvaluationCommentRepository commentRepo,
            EvaluationTrainingPlanRepository trainingPlanRepo,
            EvaluationHistoryRepository historyRepo,
            NotificationService notificationService,
            TemplateCriteriaRepository criteriaRepo,
            UserRepository userRepo,
            UserPositionRepository userPositionRepo) {
        this.recordRepo = recordRepo;
        this.scoreRepo = scoreRepo;
        this.commentRepo = commentRepo;
        this.trainingPlanRepo = trainingPlanRepo;
        this.historyRepo = historyRepo;
        this.notificationService = notificationService;
        this.criteriaRepo = criteriaRepo;
        this.userRepo = userRepo;
        this.userPositionRepo = userPositionRepo;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FETCH RECORDS
    // ═══════════════════════════════════════════════════════════════════════════

    public EvaluationRecord fetchRecordById(Long id) {
        return recordRepo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Bản đánh giá không tồn tại"));
    }

    @Transactional(readOnly = true)
    public EvaluationRecord fetchRecordByIdWithFullTemplateForUser(Long id, User requester) {
        EvaluationRecord record = fetchRecordByIdWithFullTemplate(id);
        assertCanViewRecord(record, requester);
        return record;
    }

    /**
     * Fetch record kèm đầy đủ template sections/criteria — dùng cho API chi tiết
     */
    @Transactional(readOnly = true)
    public EvaluationRecord fetchRecordByIdWithFullTemplate(Long id) {
        EvaluationRecord record = recordRepo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Bản đánh giá không tồn tại"));
        // Force load subCriteria và levels trong transaction để tránh
        // LazyInitializationException
        if (record.getTemplate() != null && record.getTemplate().getSections() != null) {
            record.getTemplate().getSections().forEach(section -> {
                if (section.getCriteria() != null) {
                    section.getCriteria().forEach(criteria -> {
                        if (criteria.getSubCriteria() != null) {
                            criteria.getSubCriteria().forEach(sub -> {
                                if (sub.getLevels() != null)
                                    sub.getLevels().size();
                            });
                        }
                        if (criteria.getLevels() != null)
                            criteria.getLevels().size();
                    });
                }
            });
        }
        return record;
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

    public List<EvaluationScore> fetchScores(Long recordId) {
        return scoreRepo.findByEvaluationRecordId(recordId);
    }

    public List<EvaluationComment> fetchComments(Long recordId) {
        return commentRepo.findByEvaluationRecordId(recordId);
    }

    public List<EvaluationTrainingPlan> fetchTrainingPlans(Long recordId) {
        return trainingPlanRepo.findByEvaluationRecordId(recordId);
    }

    /** Danh sách form chờ quản lý trực tiếp chấm */
    public List<EvaluationRecord> fetchPendingForDirectManager(String managerId) {
        return recordRepo.findByDirectManagerIdAndStatusIn(managerId,
                List.of(RecordStatus.PENDING_MANAGER_REVIEW, RecordStatus.MANAGER_REVIEWING, RecordStatus.REVISION_NEEDED));
    }

    // Lịch sử (tất cả các bản đánh giá) của quản lý trực tiếp
    public List<EvaluationRecord> fetchHistoryForDirectManager(String managerId) {
        return recordRepo.findByDirectManagerIdOrderByCreatedAtDesc(managerId);
    }

    // Lịch sử (tất cả các bản đánh giá) của quản lý gián tiếp
    public List<EvaluationRecord> fetchHistoryForIndirectManager(String approverId) {
        return recordRepo.findByIndirectManagerIdOrderByCreatedAtDesc(approverId);
    }

    /** Danh sách form chờ quản lý gián tiếp phê duyệt */
    public List<EvaluationRecord> fetchPendingForIndirectManager(String managerId) {
        return recordRepo.findByIndirectManagerIdAndStatusIn(managerId, 
            List.of(RecordStatus.PENDING_APPROVAL));
    }

    @Transactional(readOnly = true)
    public List<EvaluationRecord> fetchCompletedSummary(
            Long periodId,
            Long departmentId,
            Long companyId,
            Long sectionId,
            User requester) {
        List<EvaluationRecord> records;
        if (periodId != null) {
            records = recordRepo.findByPeriodId(periodId);
        } else {
            records = recordRepo.findAll();
        }

        records = records.stream()
                .filter(r -> r.getStatus() == RecordStatus.COMPLETED)
                .filter(r -> canViewRecord(r, requester))
                .toList();

        if (sectionId != null) {
            Set<String> userIds = new HashSet<>(userPositionRepo.findUserIdsBySectionId(sectionId));
            records = records.stream().filter(r -> userIds.contains(r.getEmployee().getId())).toList();
        } else if (departmentId != null) {
            Set<String> userIds = new HashSet<>(userPositionRepo.findUserIdsByDepartmentId(departmentId));
            records = records.stream().filter(r -> userIds.contains(r.getEmployee().getId())).toList();
        } else if (companyId != null) {
            Set<String> userIds = new HashSet<>(userPositionRepo.findUserIdsByCompanyId(companyId));
            records = records.stream().filter(r -> userIds.contains(r.getEmployee().getId())).toList();
        }

        return records;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GIAI ĐOẠN 1: NHÂN VIÊN TỰ ĐÁNH GIÁ
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Nhân viên lưu/cập nhật điểm cho một tiêu chí.
     * Có thể gọi nhiều lần (SAVE DRAFT).
     */
    @Transactional
    public EvaluationScore saveEmployeeScore(Long recordId, Long criteriaId, Double score, User employee) {
        EvaluationRecord record = fetchRecordById(recordId);
        assertEmployeePhaseOpen(record);

        // --- IDOR CHECK ---
        if (!record.getEmployee().getId().equals(employee.getId())) {
            throw new IdInvalidException("Bạn không có quyền chấm điểm bản đánh giá này");
        }

        if (record.getStatus() != RecordStatus.EMPLOYEE_DRAFTING) {
            throw new IdInvalidException("Bạn không thể chấm điểm ở trạng thái hiện tại");
        }

        validateScore(score);

        // Kiểm tra tiêu chí có sub không → nếu có thì không cho nhập tay
        TemplateCriteria criteria = criteriaRepo.findById(criteriaId)
                .orElseThrow(() -> new IdInvalidException("Tiêu chí không tồn tại"));
        assertCriteriaBelongsToRecordTemplate(record, criteria);

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
    public EvaluationRecord submitEmployeeEvaluation(Long recordId, User employee) {
        EvaluationRecord record = fetchRecordById(recordId);
        assertEmployeePhaseOpen(record);

        // --- IDOR CHECK ---
        if (!record.getEmployee().getId().equals(employee.getId())) {
            throw new IdInvalidException("Bạn không có quyền nộp bản đánh giá này");
        }

        if (record.getStatus() != RecordStatus.NOT_STARTED && record.getStatus() != RecordStatus.EMPLOYEE_DRAFTING) {
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
        sendNotification(record.getDirectManager(), "MANAGER_REVIEW_NEEDED",
                String.format("Nhân viên %s đã nộp tự đánh giá. Vui lòng chấm điểm.",
                        record.getEmployee().getName()),
                "/admin/evaluation/manager/records/" + record.getId());

        return record;
    }

    /** Nhân viên lưu nhận xét tự đánh giá (không bắt buộc) */
    @Transactional
    public EvaluationComment saveEmployeeSelfReview(Long recordId, String content, User employee) {
        EvaluationRecord record = fetchRecordById(recordId);
        assertEmployeePhaseOpen(record);

        // --- IDOR CHECK ---
        if (!record.getEmployee().getId().equals(employee.getId())) {
            throw new IdInvalidException("Bạn không có quyền nhận xét bản đánh giá này");
        }

        if (record.getStatus() != RecordStatus.NOT_STARTED && record.getStatus() != RecordStatus.EMPLOYEE_DRAFTING) {
            throw new IdInvalidException("Bạn không thể nhận xét ở trạng thái hiện tại");
        }

        // Chuyển trạng thái sang DRAFTING nếu đang NOT_STARTED
        if (record.getStatus() == RecordStatus.NOT_STARTED) {
            RecordStatus oldStatus = record.getStatus();
            record.setStatus(RecordStatus.EMPLOYEE_DRAFTING);
            recordRepo.save(record);
            saveHistory(record, oldStatus, RecordStatus.EMPLOYEE_DRAFTING, record.getEmployee(), null);
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
    public EvaluationScore saveManagerScore(Long recordId, Long criteriaId, Double score, User manager) {
        EvaluationRecord record = fetchRecordById(recordId);
        assertManagerPhaseOpen(record);

        // --- IDOR CHECK ---
        if (record.getDirectManager() == null || !record.getDirectManager().getId().equals(manager.getId())) {
            throw new IdInvalidException("Bạn không có quyền chấm điểm bản đánh giá này");
        }

        if (record.getStatus() != RecordStatus.PENDING_MANAGER_REVIEW
                && record.getStatus() != RecordStatus.MANAGER_REVIEWING
                && record.getStatus() != RecordStatus.REVISION_NEEDED) {
            throw new IdInvalidException("Quản lý không thể chấm điểm ở trạng thái hiện tại");
        }

        validateScore(score);

        TemplateCriteria criteria = criteriaRepo.findById(criteriaId)
                .orElseThrow(() -> new IdInvalidException("Tiêu chí không tồn tại"));
        assertCriteriaBelongsToRecordTemplate(record, criteria);

        List<TemplateCriteria> subs = criteriaRepo.findByParentCriteriaId(criteriaId);
        if (!subs.isEmpty()) {
            throw new IdInvalidException("Tiêu chí cha có sub-tiêu chí, điểm được tính tự động");
        }

        // Chuyển trạng thái sang MANAGER_REVIEWING nếu đang PENDING hoặc
        // REVISION_NEEDED
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
    public EvaluationRecord submitManagerReview(Long recordId, User manager) {
        EvaluationRecord record = fetchRecordById(recordId);
        assertManagerPhaseOpen(record);

        // --- IDOR CHECK ---
        if (record.getDirectManager() == null || !record.getDirectManager().getId().equals(manager.getId())) {
            throw new IdInvalidException("Bạn không có quyền gửi phê duyệt bản đánh giá này");
        }

        if (record.getStatus() != RecordStatus.PENDING_MANAGER_REVIEW
                && record.getStatus() != RecordStatus.MANAGER_REVIEWING
                && record.getStatus() != RecordStatus.REVISION_NEEDED) {
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
        sendNotification(record.getIndirectManager(), "APPROVAL_NEEDED",
                String.format("Đánh giá nhân viên %s đã được chấm điểm. Vui lòng phê duyệt.",
                        record.getEmployee().getName()),
                "/admin/evaluation/approval/records/" + record.getId());

        return record;
    }

    /** Quản lý trực tiếp lưu nhận xét */
    @Transactional
    public EvaluationComment saveManagerFeedback(Long recordId, String content, User manager) {
        EvaluationRecord record = fetchRecordById(recordId);
        assertManagerPhaseOpen(record);

        // --- IDOR CHECK ---
        if (record.getDirectManager() == null || !record.getDirectManager().getId().equals(manager.getId())) {
            throw new IdInvalidException("Bạn không có quyền nhận xét bản đánh giá này");
        }

        if (record.getStatus() != RecordStatus.PENDING_MANAGER_REVIEW
                && record.getStatus() != RecordStatus.MANAGER_REVIEWING
                && record.getStatus() != RecordStatus.REVISION_NEEDED) {
            throw new IdInvalidException("Quản lý không thể nhận xét ở trạng thái hiện tại");
        }

        // Chuyển trạng thái sang MANAGER_REVIEWING nếu đang PENDING hoặc REVISION_NEEDED
        if (record.getStatus() == RecordStatus.PENDING_MANAGER_REVIEW
                || record.getStatus() == RecordStatus.REVISION_NEEDED) {
            RecordStatus oldStatus = record.getStatus();
            record.setStatus(RecordStatus.MANAGER_REVIEWING);
            recordRepo.save(record);
            saveHistory(record, oldStatus, RecordStatus.MANAGER_REVIEWING, record.getDirectManager(), null);
        }

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
    public EvaluationTrainingPlan saveTrainingPlan(Long recordId, EvaluationTrainingPlan plan, User manager) {
        EvaluationRecord record = fetchRecordById(recordId);
        assertManagerPhaseOpen(record);

        // --- IDOR CHECK ---
        if (record.getDirectManager() == null || !record.getDirectManager().getId().equals(manager.getId())) {
            throw new IdInvalidException("Bạn không có quyền thêm kế hoạch đào tạo cho bản đánh giá này");
        }

        if (record.getStatus() != RecordStatus.PENDING_MANAGER_REVIEW
                && record.getStatus() != RecordStatus.MANAGER_REVIEWING
                && record.getStatus() != RecordStatus.REVISION_NEEDED) {
            throw new IdInvalidException("Quản lý không thể thêm kế hoạch đào tạo ở trạng thái hiện tại");
        }

        // Chuyển trạng thái sang MANAGER_REVIEWING nếu đang PENDING hoặc REVISION_NEEDED
        if (record.getStatus() == RecordStatus.PENDING_MANAGER_REVIEW
                || record.getStatus() == RecordStatus.REVISION_NEEDED) {
            RecordStatus oldStatus = record.getStatus();
            record.setStatus(RecordStatus.MANAGER_REVIEWING);
            recordRepo.save(record);
            saveHistory(record, oldStatus, RecordStatus.MANAGER_REVIEWING, record.getDirectManager(), null);
        }

        plan.setEvaluationRecord(record);
        return trainingPlanRepo.save(plan);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GIAI ĐOẠN 3: QUẢN LÝ GIÁN TIẾP PHÊ DUYỆT
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Người phê duyệt lưu điểm cho một tiêu chí.
     * Copy toàn bộ điểm của quản lý sang nếu đây là lần đầu sửa.
     */
    @Transactional
    public EvaluationScore saveApproverScore(Long recordId, Long criteriaId, Double score, User approver) {
        EvaluationRecord record = fetchRecordById(recordId);
        assertApprover(record, approver);
        assertApprovalPhaseOpen(record);

        if (record.getStatus() != RecordStatus.PENDING_APPROVAL) {
            throw new IdInvalidException("Người phê duyệt chỉ có thể chấm điểm khi bản đánh giá ở trạng thái chờ phê duyệt");
        }

        validateScore(score);

        TemplateCriteria criteria = criteriaRepo.findById(criteriaId)
                .orElseThrow(() -> new IdInvalidException("Tiêu chí không tồn tại"));
        assertCriteriaBelongsToRecordTemplate(record, criteria);

        List<TemplateCriteria> subs = criteriaRepo.findByParentCriteriaId(criteriaId);
        if (!subs.isEmpty()) {
            throw new IdInvalidException("Tiêu chí cha có sub-tiêu chí, điểm được tính tự động");
        }

        // Copy điểm MANAGER sang APPROVER nếu chưa có điểm APPROVER nào
        List<EvaluationScore> existingApproverScores = scoreRepo.findByEvaluationRecordIdAndScoredBy(recordId, ScoredBy.APPROVER);
        if (existingApproverScores.isEmpty()) {
            List<EvaluationScore> managerScores = scoreRepo.findByEvaluationRecordIdAndScoredBy(recordId, ScoredBy.MANAGER);
            for (EvaluationScore ms : managerScores) {
                saveOrUpdateScore(record, ms.getCriteria(), ScoredBy.APPROVER, ms.getScore());
            }
        }

        return saveOrUpdateScore(record, criteria, ScoredBy.APPROVER, score);
    }

    /** Phê duyệt: PENDING_APPROVAL → COMPLETED */
    @Transactional
    public EvaluationRecord approveRecord(Long recordId, User approver) {
        EvaluationRecord record = fetchRecordById(recordId);
        assertApprover(record, approver);
        assertApprovalPhaseOpen(record);

        if (record.getStatus() != RecordStatus.PENDING_APPROVAL) {
            throw new IdInvalidException("Bản đánh giá không ở trạng thái chờ phê duyệt");
        }

        RecordStatus oldStatus = record.getStatus();
        record.setStatus(RecordStatus.COMPLETED);
        record.setApprovedAt(Instant.now());
        // completedAt sẽ được set khi nhân viên xác nhận đã xem

        // Tính toán điểm của người phê duyệt (nếu có)
        List<EvaluationScore> approverScores = scoreRepo.findByEvaluationRecordIdAndScoredBy(record.getId(), ScoredBy.APPROVER);
        if (!approverScores.isEmpty()) {
            recalculateParentScores(record, ScoredBy.APPROVER);
            double approverTotal = calculateTotalScore(record, ScoredBy.APPROVER);
            record.setApproverTotalScore(approverTotal);
            record.setFinalGrade(calculateGrade(approverTotal));
        } else {
            record.setFinalGrade(calculateGrade(record.getManagerTotalScore()));
        }

        recordRepo.save(record);

        saveHistory(record, oldStatus, RecordStatus.COMPLETED, approver, null);

        // Thông báo nhân viên
        sendNotification(record.getEmployee(), "RESULT_AVAILABLE",
                "Kết quả đánh giá HQCV đã có. Nhấn để xem chi tiết.",
                "/admin/evaluation/my-records/" + record.getId());

        // Thông báo quản lý trực tiếp
        sendNotification(record.getDirectManager(), "RESULT_AVAILABLE",
                String.format("Đánh giá nhân viên %s đã được phê duyệt.",
                        record.getEmployee().getName()),
                "/admin/evaluation/manager/records/" + record.getId());

        return record;
    }

    /** Phê duyệt hàng loạt */
    @Transactional
    public List<EvaluationRecord> batchApproveRecords(List<Long> recordIds, User approver) {
        List<EvaluationRecord> approved = new java.util.ArrayList<>();
        for (Long id : recordIds) {
            approved.add(approveRecord(id, approver));
        }
        return approved;
    }

    /**
     * Nhân viên xác nhận đã xem kết quả đánh giá.
     * Chỉ áp dụng khi record đã COMPLETED.
     * Ghi nhận completedAt (thời điểm nhân viên xác nhận).
     */
    @Transactional
    public EvaluationRecord confirmEmployeeAcknowledge(Long recordId, User employee) {
        EvaluationRecord record = fetchRecordById(recordId);

        if (record.getStatus() != RecordStatus.COMPLETED) {
            throw new IdInvalidException("Bản đánh giá chưa được phê duyệt hoàn tất");
        }

        // Kiểm tra đúng nhân viên
        if (!record.getEmployee().getId().equals(employee.getId())) {
            throw new IdInvalidException("Bạn không có quyền xác nhận bản đánh giá này");
        }

        // Set completedAt là thời điểm nhân viên xác nhận đã xem
        record.setCompletedAt(Instant.now());
        recordRepo.save(record);

        // Thông báo quản lý trực tiếp biết nhân viên đã xem
        sendNotification(record.getDirectManager(), "RESULT_AVAILABLE",
                String.format("Nhân viên %s đã xác nhận xem kết quả đánh giá.",
                        record.getEmployee().getName()),
                "/admin/evaluation/manager/records/" + record.getId());

        return record;
    }

    /**
     * Trả lại: PENDING_APPROVAL → REVISION_NEEDED.
     * Bắt buộc phải có lý do.
     */
    @Transactional
    public EvaluationRecord rejectRecord(Long recordId, String reason, User rejector) {
        EvaluationRecord record = fetchRecordById(recordId);
        assertApprover(record, rejector);
        assertApprovalPhaseOpen(record);

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
        sendNotification(record.getDirectManager(), "REVISION_NEEDED",
                String.format("Đánh giá nhân viên %s đã bị trả lại. Lý do: %s",
                        record.getEmployee().getName(), reason),
                "/admin/evaluation/manager/records/" + record.getId());

        return record;
    }

    /** Đếm số lần bị trả lại */
    public long countRejections(Long recordId, User requester) {
        EvaluationRecord record = fetchRecordById(recordId);
        assertCanViewRecord(record, requester);
        return historyRepo.countByEvaluationRecordIdAndToStatus(recordId, RecordStatus.REVISION_NEEDED);
    }

    /** Admin gia hạn deadline riêng cho một hoặc nhiều bản đánh giá. */
    @Transactional
    public List<EvaluationRecord> extendRecordDeadlines(
            List<Long> recordIds,
            ExtendRecordDeadlineRequest.Phase phase,
            Instant deadline,
            String reason,
            boolean cascade,
            User performer) {
        if (recordIds == null || recordIds.isEmpty()) {
            throw new IdInvalidException("Danh sách bản đánh giá không được để trống");
        }
        if (phase == null) {
            throw new IdInvalidException("Giai đoạn gia hạn không hợp lệ");
        }
        if (deadline == null || !deadline.isAfter(Instant.now())) {
            throw new IdInvalidException("Hạn mới phải lớn hơn thời điểm hiện tại");
        }

        List<EvaluationRecord> records = recordRepo.findAllById(recordIds);
        if (records.size() != new HashSet<>(recordIds).size()) {
            throw new IdInvalidException("Một hoặc nhiều bản đánh giá không tồn tại");
        }

        for (EvaluationRecord record : records) {
            if (record.getPeriod() == null || record.getPeriod().getStatus() != PeriodStatus.ACTIVE) {
                throw new IdInvalidException("Chỉ có thể gia hạn bản đánh giá thuộc kỳ đang hoạt động");
            }
            if (record.getStatus() == RecordStatus.COMPLETED) {
                throw new IdInvalidException("Không thể gia hạn bản đánh giá đã hoàn tất");
            }

            switch (phase) {
                case EMPLOYEE -> extendEmployeeDeadline(record, deadline, cascade);
                case MANAGER -> extendManagerDeadline(record, deadline, cascade);
                case APPROVAL -> extendApprovalDeadline(record, deadline);
            }

            String note = String.format("Gia hạn giai đoạn %s tới %s%s",
                    phase,
                    deadline,
                    reason != null && !reason.isBlank() ? ". Lý do: " + reason.trim() : "");
            saveHistory(record, record.getStatus(), record.getStatus(), performer, note);
        }

        return recordRepo.saveAll(records);
    }

    /** Lịch sử thay đổi trạng thái */
    public List<EvaluationHistory> fetchHistory(Long recordId, User requester) {
        EvaluationRecord record = fetchRecordById(recordId);
        assertCanViewRecord(record, requester);
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
        // KHÔNG nhân với sectionWeight vì criteriaWeight đã là trọng số tuyệt đối (theo frontend)
        double criteriaWeight = criteria.getWeight();

        // Nếu là sub-tiêu chí → lấy weight của tiêu chí cha
        if (criteria.getParentCriteria() != null) {
            criteriaWeight = criteria.getParentCriteria().getWeight();
            // Sub-tiêu chí chia đều weight cha (trung bình)
            List<TemplateCriteria> siblings = criteriaRepo.findByParentCriteriaId(
                    criteria.getParentCriteria().getId());
            // weighted score cho sub = score × parentWeight / numSubs
            evalScore.setWeightedScore(score * criteriaWeight / siblings.size());
        } else {
            evalScore.setWeightedScore(score * criteriaWeight);
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
     * < 3.0 → E
     * 3.0–3.5 → D
     * 3.5–4.0 → C
     * 4.0–4.5 → B
     * > 4.5 → A
     */
    private String calculateGrade(double totalScore) {
        if (totalScore > 4.5)
            return "A";
        if (totalScore > 4.0)
            return "B";
        if (totalScore > 3.5)
            return "C";
        if (totalScore >= 3.0)
            return "D";
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

    private void assertCanViewRecord(EvaluationRecord record, User requester) {
        if (!canViewRecord(record, requester)) {
            throw new IdInvalidException("Bạn không có quyền xem bản đánh giá này");
        }
    }

    private boolean canViewRecord(EvaluationRecord record, User requester) {
        if (record == null || requester == null) {
            return false;
        }

        String requesterId = requester.getId();
        if (record.getEmployee() != null && requesterId.equals(record.getEmployee().getId())) {
            return true;
        }
        if (record.getDirectManager() != null && requesterId.equals(record.getDirectManager().getId())) {
            return true;
        }
        if (record.getIndirectManager() != null && requesterId.equals(record.getIndirectManager().getId())) {
            return true;
        }

        UserScopeContext.UserScope scope = UserScopeContext.get();
        if (scope == null) {
            return false;
        }
        if (scope.isSuperAdmin() || scope.isAdminLevel()) {
            return true;
        }

        String employeeId = record.getEmployee() != null ? record.getEmployee().getId() : null;
        if (employeeId == null) {
            return false;
        }

        if (scope.companyIds() != null && !scope.companyIds().isEmpty()) {
            for (Long companyId : scope.companyIds()) {
                if (userPositionRepo.findUserIdsByCompanyId(companyId).contains(employeeId)) {
                    return true;
                }
            }
        }

        if (!scope.isCompanyLevel() && scope.departmentIds() != null && !scope.departmentIds().isEmpty()) {
            for (Long departmentId : scope.departmentIds()) {
                if (userPositionRepo.findUserIdsByDepartmentId(departmentId).contains(employeeId)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void assertApprover(EvaluationRecord record, User approver) {
        if (record.getIndirectManager() == null || approver == null
                || !record.getIndirectManager().getId().equals(approver.getId())) {
            throw new IdInvalidException("Bạn không có quyền phê duyệt bản đánh giá này");
        }
    }

    private void assertCriteriaBelongsToRecordTemplate(EvaluationRecord record, TemplateCriteria criteria) {
        Long recordTemplateId = record.getTemplate() != null ? record.getTemplate().getId() : null;
        Long criteriaTemplateId = criteria.getSection() != null
                && criteria.getSection().getTemplate() != null
                        ? criteria.getSection().getTemplate().getId()
                        : null;

        if (recordTemplateId == null || !Objects.equals(recordTemplateId, criteriaTemplateId)) {
            throw new IdInvalidException("Tiêu chí không thuộc mẫu đánh giá của bản đánh giá này");
        }
    }

    private void assertEmployeePhaseOpen(EvaluationRecord record) {
        EvaluationPeriod period = record.getPeriod();
        Instant now = Instant.now();

        if (period == null || period.getStatus() != PeriodStatus.ACTIVE) {
            throw new IdInvalidException("Kỳ đánh giá hiện không mở");
        }
        if (period.getEmployeeStartDate() != null && now.isBefore(period.getEmployeeStartDate())) {
            throw new IdInvalidException("Chưa đến thời gian nhân viên tự đánh giá");
        }
        Instant employeeDeadline = record.getEmployeeDeadlineOverride() != null
                ? record.getEmployeeDeadlineOverride()
                : period.getEmployeeDeadline();
        if (employeeDeadline != null && now.isAfter(employeeDeadline)) {
            throw new IdInvalidException("Đã quá hạn nhân viên tự đánh giá");
        }
    }

    private void assertManagerPhaseOpen(EvaluationRecord record) {
        EvaluationPeriod period = record.getPeriod();
        Instant now = Instant.now();

        if (period == null || period.getStatus() != PeriodStatus.ACTIVE) {
            throw new IdInvalidException("Kỳ đánh giá hiện không mở");
        }
        Instant managerDeadline = record.getManagerDeadlineOverride() != null
                ? record.getManagerDeadlineOverride()
                : period.getManagerDeadline();
        if (managerDeadline != null && now.isAfter(managerDeadline)) {
            throw new IdInvalidException("Đã quá hạn quản lý chấm điểm");
        }
    }

    private void assertApprovalPhaseOpen(EvaluationRecord record) {
        EvaluationPeriod period = record.getPeriod();
        Instant now = Instant.now();

        if (period == null || period.getStatus() != PeriodStatus.ACTIVE) {
            throw new IdInvalidException("Kỳ đánh giá hiện không mở");
        }
        Instant approvalDeadline = record.getApprovalDeadlineOverride() != null
                ? record.getApprovalDeadlineOverride()
                : period.getApprovalDeadline();
        if (approvalDeadline != null && now.isAfter(approvalDeadline)) {
            throw new IdInvalidException("Đã quá hạn phê duyệt đánh giá");
        }
    }

    private Instant getEffectiveEmployeeDeadline(EvaluationRecord record) {
        return record.getEmployeeDeadlineOverride() != null
                ? record.getEmployeeDeadlineOverride()
                : record.getPeriod().getEmployeeDeadline();
    }

    private Instant getEffectiveManagerDeadline(EvaluationRecord record) {
        return record.getManagerDeadlineOverride() != null
                ? record.getManagerDeadlineOverride()
                : record.getPeriod().getManagerDeadline();
    }

    private Instant getEffectiveApprovalDeadline(EvaluationRecord record) {
        return record.getApprovalDeadlineOverride() != null
                ? record.getApprovalDeadlineOverride()
                : record.getPeriod().getApprovalDeadline();
    }

    private String formatInstant(Instant instant) {
        if (instant == null) return "";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        return formatter.format(instant);
    }

    private void extendEmployeeDeadline(EvaluationRecord record, Instant deadline, boolean cascade) {
        if (record.getEmployeeSubmittedAt() != null 
                || (record.getStatus() != RecordStatus.NOT_STARTED 
                    && record.getStatus() != RecordStatus.EMPLOYEE_DRAFTING)) {
            throw new IdInvalidException("Chỉ có thể gia hạn nhân viên khi bản đánh giá còn ở bước nhân viên tự đánh giá");
        }
        
        if (cascade) {
            Instant oldEmployeeDeadline = getEffectiveEmployeeDeadline(record);
            if (oldEmployeeDeadline != null && deadline.isAfter(oldEmployeeDeadline)) {
                java.time.Duration diff = java.time.Duration.between(oldEmployeeDeadline, deadline);
                
                // Tự động tịnh tiến hạn của Quản lý
                Instant oldManagerDeadline = getEffectiveManagerDeadline(record);
                if (oldManagerDeadline != null) {
                    record.setManagerDeadlineOverride(oldManagerDeadline.plus(diff));
                }
                
                // Tự động tịnh tiến hạn của Người duyệt
                Instant oldApprovalDeadline = getEffectiveApprovalDeadline(record);
                if (oldApprovalDeadline != null) {
                    record.setApprovalDeadlineOverride(oldApprovalDeadline.plus(diff));
                }
            }
        }
        
        record.setEmployeeDeadlineOverride(deadline);

        // Kiểm tra tính hợp lệ về mặt logic sau khi tịnh tiến
        Instant finalEmployee = getEffectiveEmployeeDeadline(record);
        Instant finalManager = getEffectiveManagerDeadline(record);
        if (finalEmployee != null && finalManager != null && finalEmployee.isAfter(finalManager)) {
            throw new IdInvalidException(String.format(
                    "Lỗi logic: Hạn mới của Nhân viên (%s) không được trễ hơn hạn của Quản lý (%s). Vui lòng tích chọn 'Tự động tịnh tiến các hạn chót tiếp theo'.",
                    formatInstant(finalEmployee), formatInstant(finalManager)));
        }
    }

    private void extendManagerDeadline(EvaluationRecord record, Instant deadline, boolean cascade) {
        if (record.getManagerSubmittedAt() != null
                || (record.getStatus() != RecordStatus.PENDING_MANAGER_REVIEW
                        && record.getStatus() != RecordStatus.MANAGER_REVIEWING
                        && record.getStatus() != RecordStatus.REVISION_NEEDED)) {
            throw new IdInvalidException("Chỉ có thể gia hạn quản lý khi bản đánh giá đang ở bước quản lý chấm/sửa");
        }
        
        if (cascade) {
            Instant oldManagerDeadline = getEffectiveManagerDeadline(record);
            if (oldManagerDeadline != null && deadline.isAfter(oldManagerDeadline)) {
                java.time.Duration diff = java.time.Duration.between(oldManagerDeadline, deadline);
                
                // Tự động tịnh tiến hạn của Người duyệt
                Instant oldApprovalDeadline = getEffectiveApprovalDeadline(record);
                if (oldApprovalDeadline != null) {
                    record.setApprovalDeadlineOverride(oldApprovalDeadline.plus(diff));
                }
            }
        }
        
        record.setManagerDeadlineOverride(deadline);

        // Kiểm tra tính hợp lệ về mặt logic sau khi tịnh tiến
        Instant finalEmployee = getEffectiveEmployeeDeadline(record);
        Instant finalManager = getEffectiveManagerDeadline(record);
        Instant finalApproval = getEffectiveApprovalDeadline(record);

        if (finalEmployee != null && finalManager != null && finalManager.isBefore(finalEmployee)) {
            throw new IdInvalidException(String.format(
                    "Lỗi logic: Hạn mới của Quản lý (%s) không được sớm hơn hạn của Nhân viên (%s).",
                    formatInstant(finalManager), formatInstant(finalEmployee)));
        }

        if (finalManager != null && finalApproval != null && finalManager.isAfter(finalApproval)) {
            throw new IdInvalidException(String.format(
                    "Lỗi logic: Hạn mới của Quản lý (%s) không được trễ hơn hạn của Người duyệt (%s). Vui lòng tích chọn 'Tự động tịnh tiến các hạn chót tiếp theo'.",
                    formatInstant(finalManager), formatInstant(finalApproval)));
        }
    }

    private void extendApprovalDeadline(EvaluationRecord record, Instant deadline) {
        if (record.getApprovedAt() != null || record.getStatus() != RecordStatus.PENDING_APPROVAL) {
            throw new IdInvalidException("Chỉ có thể gia hạn phê duyệt khi bản đánh giá đang chờ phê duyệt");
        }
        
        record.setApprovalDeadlineOverride(deadline);

        // Kiểm tra tính hợp lệ về mặt logic sau khi tịnh tiến
        Instant finalManager = getEffectiveManagerDeadline(record);
        Instant finalApproval = getEffectiveApprovalDeadline(record);

        if (finalManager != null && finalApproval != null && finalApproval.isBefore(finalManager)) {
            throw new IdInvalidException(String.format(
                    "Lỗi logic: Hạn mới của Người duyệt (%s) không được sớm hơn hạn của Quản lý (%s).",
                    formatInstant(finalApproval), formatInstant(finalManager)));
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
                    List<TemplateCriteria> sectionCriteria = criteriaRepo
                            .findBySectionIdOrderByDisplayOrderAsc(section.getId());
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

    private void sendNotification(User recipient, String type, String content, String actionLink) {
        if (recipient == null) return;
        notificationService.sendNotification(recipient.getId(), "EVALUATION", type, content, actionLink);
    }
}
