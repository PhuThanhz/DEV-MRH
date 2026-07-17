package vn.system.app.modules.evaluation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.evaluation.domain.*;
import vn.system.app.modules.evaluation.domain.request.BatchApproveRequest;
import vn.system.app.modules.evaluation.domain.request.ExtendRecordDeadlineRequest;
import vn.system.app.modules.evaluation.domain.request.ReassignEvaluatorRequest;
import vn.system.app.modules.evaluation.domain.request.ScoreRequest;
import vn.system.app.modules.evaluation.domain.request.TrainingPlanRequest;
import vn.system.app.modules.evaluation.domain.response.ResEvaluationHistoryDTO;
import vn.system.app.modules.evaluation.domain.response.ResEvaluationRecordDTO;
import vn.system.app.modules.evaluation.domain.response.ResEvaluationTaskCountsDTO;
import vn.system.app.modules.evaluation.service.EvaluationMapper;
import vn.system.app.modules.evaluation.service.EvaluationRecordService;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.error.IdInvalidException;

/**
 * API Đánh giá HQCV — dùng chung cho nhân viên, quản lý trực tiếp, quản lý gián
 * tiếp.
 * Phân quyền ở logic layer dựa trên record owner.
 */
@RestController
@RequestMapping("/api/v1/evaluation")
public class EvaluationRecordController {

    private final EvaluationRecordService recordService;
    private final EvaluationMapper mapper;
    private final UserRepository userRepo;

    public EvaluationRecordController(
            EvaluationRecordService recordService,
            EvaluationMapper mapper,
            UserRepository userRepo) {
        this.recordService = recordService;
        this.mapper = mapper;
        this.userRepo = userRepo;
    }

    // ═══════════════ FETCH RECORDS ═══════════════

    @GetMapping("/records/{id}")
    @ApiMessage("Chi tiết bản đánh giá")
    public ResponseEntity<ResEvaluationRecordDTO> fetchRecord(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        EvaluationRecord record = recordService.fetchRecordByIdWithFullTemplateForUser(id, currentUser);
        return ResponseEntity.ok(mapper.toResEvaluationRecordDTO(
                record,
                recordService.fetchScores(id),
                recordService.fetchComments(id),
                recordService.fetchTrainingPlans(id)
        ));
    }

    /** Nhân viên: danh sách bản đánh giá của mình (lịch sử) */
    @GetMapping("/my-records")
    @ApiMessage("Danh sách bản đánh giá của tôi")
    public ResponseEntity<List<ResEvaluationRecordDTO>> fetchMyRecords() {
        String currentUserId = getCurrentUserId();
        return ResponseEntity.ok(mapper.toResEvaluationRecordSummaryDTOs(
                recordService.fetchRecordsByEmployee(currentUserId)));
    }

    /** Quản trị: danh sách toàn bộ bản đánh giá trong hệ thống. */
    @GetMapping("/records")
    @ApiMessage("Danh sách toàn bộ bản đánh giá")
    public ResponseEntity<List<ResEvaluationRecordDTO>> fetchAllRecords() {
        return ResponseEntity.ok(mapper.toResEvaluationRecordSummaryDTOs(
                recordService.fetchAllRecordsForAdministration()));
    }

    @GetMapping("/task-counts")
    @ApiMessage("Số công việc đánh giá đang chờ")
    public ResponseEntity<ResEvaluationTaskCountsDTO> fetchTaskCounts() {
        return ResponseEntity.ok(recordService.fetchTaskCounts(getCurrentUserId()));
    }

    /** Quản lý trực tiếp: danh sách nhân viên trong kỳ */
    @GetMapping("/manager/periods/{periodId}/records")
    @ApiMessage("Danh sách bản đánh giá cho quản lý trực tiếp")
    public ResponseEntity<List<ResEvaluationRecordDTO>> fetchForDirectManager(@PathVariable Long periodId) {
        String currentUserId = getCurrentUserId();
        return ResponseEntity.ok(mapper.toResEvaluationRecordSummaryDTOs(
                recordService.fetchRecordsForDirectManager(periodId, currentUserId)));
    }

    @GetMapping("/manager/pending")
    @ApiMessage("Danh sách chờ quản lý trực tiếp chấm")
    public ResponseEntity<List<ResEvaluationRecordDTO>> fetchPendingForManager() {
        String currentUserId = getCurrentUserId();
        return ResponseEntity.ok(mapper.toResEvaluationRecordSummaryDTOs(
                recordService.fetchPendingForDirectManager(currentUserId)));
    }

    @GetMapping("/manager/records")
    @ApiMessage("Tất cả bản đánh giá của quản lý trực tiếp")
    public ResponseEntity<List<ResEvaluationRecordDTO>> fetchAllForManager() {
        String currentUserId = getCurrentUserId();
        return ResponseEntity.ok(mapper.toResEvaluationRecordSummaryDTOs(
                recordService.fetchHistoryForDirectManager(currentUserId)));
    }

    /** Quản lý gián tiếp: danh sách nhân viên trong kỳ */
    @GetMapping("/approval/periods/{periodId}/records")
    @ApiMessage("Danh sách bản đánh giá cho quản lý gián tiếp")
    public ResponseEntity<List<ResEvaluationRecordDTO>> fetchForIndirectManager(@PathVariable Long periodId) {
        String currentUserId = getCurrentUserId();
        return ResponseEntity.ok(mapper.toResEvaluationRecordSummaryDTOs(
                recordService.fetchRecordsForIndirectManager(periodId, currentUserId)));
    }

    @GetMapping("/approval/pending")
    @ApiMessage("Danh sách chờ phê duyệt")
    public ResponseEntity<List<ResEvaluationRecordDTO>> fetchPendingForApproval() {
        String currentUserId = getCurrentUserId();
        return ResponseEntity.ok(mapper.toResEvaluationRecordSummaryDTOs(
                recordService.fetchPendingForIndirectManager(currentUserId)));
    }

    @GetMapping("/approval/records")
    @ApiMessage("Tất cả bản đánh giá của người phê duyệt")
    public ResponseEntity<List<ResEvaluationRecordDTO>> fetchAllForApprover() {
        String currentUserId = getCurrentUserId();
        return ResponseEntity.ok(mapper.toResEvaluationRecordSummaryDTOs(
                recordService.fetchHistoryForIndirectManager(currentUserId)));
    }

    @GetMapping("/summary/completed")
    @ApiMessage("Danh sách tổng hợp đánh giá hoàn tất")
    public ResponseEntity<vn.system.app.common.response.ResultPaginationDTO> fetchCompletedSummary(
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) String filterGrade,
            org.springframework.data.domain.Pageable pageable) {
        User currentUser = getCurrentUser();
        org.springframework.data.domain.Page<EvaluationRecord> page = recordService.fetchCompletedSummary(
                periodId, departmentId, companyId, sectionId, searchText, filterGrade, currentUser, pageable);
        
        vn.system.app.common.response.ResultPaginationDTO res = new vn.system.app.common.response.ResultPaginationDTO();
        vn.system.app.common.response.ResultPaginationDTO.Meta meta = new vn.system.app.common.response.ResultPaginationDTO.Meta();
        meta.setPage(page.getNumber() + 1);
        meta.setPageSize(page.getSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        res.setMeta(meta);
        res.setResult(mapper.toResEvaluationRecordSummaryDTOs(page.getContent()));
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/records/deadline-extension")
    @ApiMessage("Gia hạn deadline riêng cho bản đánh giá")
    public ResponseEntity<List<ResEvaluationRecordDTO>> extendRecordDeadline(
            @Valid @RequestBody ExtendRecordDeadlineRequest body) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(recordService.extendRecordDeadlines(
                body.getRecordIds(),
                body.getPhase(),
                body.getDeadline(),
                body.getReason(),
                body.isCascade(),
                currentUser).stream().map(mapper::toResEvaluationRecordDTO).toList());
    }

    @PatchMapping("/records/reassign-evaluator")
    @ApiMessage("Điều chuyển người chấm/duyệt bản đánh giá")
    public ResponseEntity<List<ResEvaluationRecordDTO>> reassignEvaluators(
            @Valid @RequestBody ReassignEvaluatorRequest body) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(recordService.reassignEvaluators(
                body.getRecordIds(),
                body.getEvaluatorRole(),
                body.getNewEvaluatorUserId(),
                body.getReason(),
                currentUser).stream().map(mapper::toResEvaluationRecordDTO).toList());
    }

    // ═══════════════ GIAI ĐOẠN 1: NHÂN VIÊN TỰ ĐÁNH GIÁ ═══════════════

    @PostMapping("/records/{recordId}/employee-scores")
    @ApiMessage("Nhân viên chấm điểm tiêu chí")
    public ResponseEntity<ResEvaluationRecordDTO.ResScoreDTO> saveEmployeeScore(
            @PathVariable Long recordId,
            @Valid @RequestBody ScoreRequest body) {
        User employee = getCurrentUser();
        return ResponseEntity.ok(mapper.toResScoreDTO(
                recordService.saveEmployeeScore(recordId, body.getCriteriaId(), body.getScore(), employee)));
    }

    @PostMapping("/records/{recordId}/employee-submit")
    @ApiMessage("Nhân viên nộp tự đánh giá")
    public ResponseEntity<ResEvaluationRecordDTO> submitEmployeeEvaluation(@PathVariable Long recordId) {
        User employee = getCurrentUser();
        return ResponseEntity.ok(mapper.toResEvaluationRecordDTO(recordService.submitEmployeeEvaluation(recordId, employee)));
    }

    @PostMapping("/records/{recordId}/self-review")
    @ApiMessage("Nhân viên lưu tự nhận xét")
    public ResponseEntity<ResEvaluationRecordDTO.ResCommentDTO> saveSelfReview(
            @PathVariable Long recordId,
            @RequestBody Map<String, String> body) {
        User employee = getCurrentUser();
        return ResponseEntity.ok(
                mapper.toResCommentDTO(recordService.saveEmployeeSelfReview(recordId, body.get("content"), employee)));
    }

    // ═══════════════ GIAI ĐOẠN 2: QUẢN LÝ TRỰC TIẾP CHẤM ĐIỂM ═══════════════

    @PostMapping("/records/{recordId}/manager-scores")
    @ApiMessage("Quản lý chấm điểm tiêu chí")
    public ResponseEntity<ResEvaluationRecordDTO.ResScoreDTO> saveManagerScore(
            @PathVariable Long recordId,
            @Valid @RequestBody ScoreRequest body) {
        User manager = getCurrentUser();
        return ResponseEntity.ok(mapper.toResScoreDTO(
                recordService.saveManagerScore(recordId, body.getCriteriaId(), body.getScore(), manager)));
    }

    @PostMapping("/records/{recordId}/manager-submit")
    @ApiMessage("Quản lý gửi phê duyệt")
    public ResponseEntity<ResEvaluationRecordDTO> submitManagerReview(@PathVariable Long recordId) {
        User manager = getCurrentUser();
        return ResponseEntity.ok(mapper.toResEvaluationRecordDTO(recordService.submitManagerReview(recordId, manager)));
    }

    @PostMapping("/records/{recordId}/manager-feedback")
    @ApiMessage("Quản lý lưu nhận xét")
    public ResponseEntity<ResEvaluationRecordDTO.ResCommentDTO> saveManagerFeedback(
            @PathVariable Long recordId,
            @RequestBody Map<String, String> body) {
        User manager = getCurrentUser();
        return ResponseEntity
                .ok(mapper.toResCommentDTO(recordService.saveManagerFeedback(recordId, body.get("content"), manager)));
    }

    @PostMapping("/records/{recordId}/training-plans")
    @ApiMessage("Quản lý lưu kế hoạch đào tạo")
    public ResponseEntity<ResEvaluationRecordDTO.ResTrainingPlanDTO> saveTrainingPlan(
            @PathVariable Long recordId,
            @Valid @RequestBody TrainingPlanRequest plan) {
        User manager = getCurrentUser();
        return ResponseEntity.ok(mapper.toResTrainingPlanDTO(recordService.saveTrainingPlan(recordId, plan, manager)));
    }

    // ═══════════════ GIAI ĐOẠN 3: QUẢN LÝ GIÁN TIẾP PHÊ DUYỆT ═══════════════

    @PostMapping("/records/{recordId}/approver-scores")
    @ApiMessage("Người phê duyệt chấm điểm tiêu chí")
    public ResponseEntity<ResEvaluationRecordDTO.ResScoreDTO> saveApproverScore(
            @PathVariable Long recordId,
            @Valid @RequestBody ScoreRequest body) {
        User approver = getCurrentUser();
        return ResponseEntity.ok(mapper.toResScoreDTO(
                recordService.saveApproverScore(recordId, body.getCriteriaId(), body.getScore(), approver)));
    }

    @PostMapping("/records/{recordId}/approve")
    @ApiMessage("Phê duyệt bản đánh giá")
    public ResponseEntity<ResEvaluationRecordDTO> approveRecord(
            @PathVariable Long recordId,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        User approver = getCurrentUser();
        String overrideReason = body != null ? body.get("overrideReason") : null;
        return ResponseEntity.ok(mapper.toResEvaluationRecordDTO(recordService.approveRecord(recordId, overrideReason, approver)));
    }

    @PostMapping("/records/batch-approve")
    @ApiMessage("Phê duyệt hàng loạt")
    public ResponseEntity<vn.system.app.modules.evaluation.domain.response.BatchApproveResponse> batchApproveRecords(
            @Valid @RequestBody BatchApproveRequest req) {
        User approver = getCurrentUser();
        return ResponseEntity.ok(recordService.batchApproveRecords(req.getRecordIds(), approver));
    }

    @PostMapping("/records/{recordId}/reject")
    @ApiMessage("Trả lại bản đánh giá")
    public ResponseEntity<ResEvaluationRecordDTO> rejectRecord(
            @PathVariable Long recordId,
            @RequestBody Map<String, String> body) {
        User rejector = getCurrentUser();
        return ResponseEntity.ok(
                mapper.toResEvaluationRecordDTO(recordService.rejectRecord(recordId, body.get("reason"), rejector)));
    }

    @PostMapping("/records/{recordId}/employee-confirm")
    @ApiMessage("Nhân viên xác nhận đã xem kết quả")
    public ResponseEntity<ResEvaluationRecordDTO> confirmEmployeeAcknowledge(@PathVariable Long recordId) {
        User employee = getCurrentUser();
        return ResponseEntity
                .ok(mapper.toResEvaluationRecordDTO(recordService.confirmEmployeeAcknowledge(recordId, employee)));
    }

    // ═══════════════ LỊCH SỬ & BÁO CÁO ═══════════════

    @GetMapping("/records/{recordId}/history")
    @ApiMessage("Lịch sử thay đổi trạng thái")
    public ResponseEntity<List<ResEvaluationHistoryDTO>> fetchHistory(@PathVariable Long recordId) {
        User currentUser = getCurrentUser();
        return ResponseEntity
                .ok(recordService.fetchHistory(recordId, currentUser).stream()
                        .map(mapper::toResEvaluationHistoryDTO).toList());
    }

    @GetMapping("/records/{recordId}/score-audits")
    @ApiMessage("Lịch sử thay đổi điểm số")
    public ResponseEntity<List<EvaluationScoreAudit>> fetchScoreAudits(@PathVariable Long recordId) {
        User currentUser = getCurrentUser();
        return ResponseEntity.ok(recordService.fetchScoreAudits(recordId, currentUser));
    }

    @GetMapping("/records/{recordId}/rejection-count")
    @ApiMessage("Số lần bị trả lại")
    public ResponseEntity<Map<String, Long>> countRejections(@PathVariable Long recordId) {
        User currentUser = getCurrentUser();
        long count = recordService.countRejections(recordId, currentUser);
        return ResponseEntity.ok(Map.of("rejectionCount", count));
    }

    @GetMapping("/periods/{periodId}/status-distribution")
    @ApiMessage("Phân bổ trạng thái trong kỳ")
    public ResponseEntity<List<Object[]>> getStatusDistribution(@PathVariable Long periodId) {
        return ResponseEntity.ok(recordService.getStatusDistribution(periodId));
    }

    @GetMapping("/periods/{periodId}/grade-distribution")
    @ApiMessage("Phân bổ xếp loại A/B/C/D/E trong kỳ")
    public ResponseEntity<List<Object[]>> getGradeDistribution(@PathVariable Long periodId) {
        return ResponseEntity.ok(recordService.getGradeDistribution(periodId));
    }

    // ═══════════════ HELPERS ═══════════════

    private String getCurrentUserId() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Chưa đăng nhập"));
        User user = userRepo.findByEmail(email);
        if (user == null)
            throw new IdInvalidException("Người dùng không tồn tại");
        return user.getId();
    }

    private User getCurrentUser() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Chưa đăng nhập"));
        User user = userRepo.findByEmail(email);
        if (user == null)
            throw new IdInvalidException("Người dùng không tồn tại");
        return user;
    }
}
