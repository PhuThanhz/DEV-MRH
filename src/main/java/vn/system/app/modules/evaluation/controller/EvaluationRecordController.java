package vn.system.app.modules.evaluation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.evaluation.domain.*;
import vn.system.app.modules.evaluation.domain.response.ResEvaluationHistoryDTO;
import vn.system.app.modules.evaluation.domain.response.ResEvaluationRecordDTO;
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
        EvaluationRecord record = recordService.fetchRecordByIdWithFullTemplate(id);
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
        return ResponseEntity.ok(recordService.fetchRecordsByEmployee(currentUserId).stream()
                .map(mapper::toResEvaluationRecordDTO).toList());
    }

    /** Quản lý trực tiếp: danh sách nhân viên trong kỳ */
    @GetMapping("/manager/periods/{periodId}/records")
    @ApiMessage("Danh sách bản đánh giá cho quản lý trực tiếp")
    public ResponseEntity<List<ResEvaluationRecordDTO>> fetchForDirectManager(@PathVariable Long periodId) {
        String currentUserId = getCurrentUserId();
        return ResponseEntity.ok(recordService.fetchRecordsForDirectManager(periodId, currentUserId).stream()
                .map(mapper::toResEvaluationRecordDTO).toList());
    }

    @GetMapping("/manager/pending")
    @ApiMessage("Danh sách chờ quản lý trực tiếp chấm")
    public ResponseEntity<List<ResEvaluationRecordDTO>> fetchPendingForManager() {
        String currentUserId = getCurrentUserId();
        return ResponseEntity.ok(recordService.fetchPendingForDirectManager(currentUserId).stream()
                .map(mapper::toResEvaluationRecordDTO).toList());
    }

    @GetMapping("/manager/records")
    @ApiMessage("Tất cả bản đánh giá của quản lý trực tiếp")
    public ResponseEntity<List<ResEvaluationRecordDTO>> fetchAllForManager() {
        String currentUserId = getCurrentUserId();
        return ResponseEntity.ok(recordService.fetchHistoryForDirectManager(currentUserId).stream()
                .map(mapper::toResEvaluationRecordDTO).toList());
    }

    /** Quản lý gián tiếp: danh sách nhân viên trong kỳ */
    @GetMapping("/approval/periods/{periodId}/records")
    @ApiMessage("Danh sách bản đánh giá cho quản lý gián tiếp")
    public ResponseEntity<List<ResEvaluationRecordDTO>> fetchForIndirectManager(@PathVariable Long periodId) {
        String currentUserId = getCurrentUserId();
        return ResponseEntity.ok(recordService.fetchRecordsForIndirectManager(periodId, currentUserId).stream()
                .map(mapper::toResEvaluationRecordDTO).toList());
    }

    @GetMapping("/approval/pending")
    @ApiMessage("Danh sách chờ phê duyệt")
    public ResponseEntity<List<ResEvaluationRecordDTO>> fetchPendingForApproval() {
        String currentUserId = getCurrentUserId();
        return ResponseEntity.ok(recordService.fetchPendingForIndirectManager(currentUserId).stream()
                .map(mapper::toResEvaluationRecordDTO).toList());
    }

    @GetMapping("/approval/records")
    @ApiMessage("Tất cả bản đánh giá của người phê duyệt")
    public ResponseEntity<List<ResEvaluationRecordDTO>> fetchAllForApprover() {
        String currentUserId = getCurrentUserId();
        return ResponseEntity.ok(recordService.fetchHistoryForIndirectManager(currentUserId).stream()
                .map(mapper::toResEvaluationRecordDTO).toList());
    }

    @GetMapping("/summary/completed")
    @ApiMessage("Danh sách tổng hợp đánh giá hoàn tất")
    public ResponseEntity<List<ResEvaluationRecordDTO>> fetchCompletedSummary(
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long companyId) {
        List<EvaluationRecord> records = recordService.fetchCompletedSummary(periodId, departmentId, companyId);
        return ResponseEntity.ok(records.stream().map(mapper::toResEvaluationRecordDTO).toList());
    }

    // ═══════════════ GIAI ĐOẠN 1: NHÂN VIÊN TỰ ĐÁNH GIÁ ═══════════════

    @PostMapping("/records/{recordId}/employee-scores")
    @ApiMessage("Nhân viên chấm điểm tiêu chí")
    public ResponseEntity<ResEvaluationRecordDTO.ResScoreDTO> saveEmployeeScore(
            @PathVariable Long recordId,
            @RequestBody Map<String, Object> body) {
        Long criteriaId = Long.valueOf(body.get("criteriaId").toString());
        Double score = Double.valueOf(body.get("score").toString());
        User employee = getCurrentUser();
        return ResponseEntity.ok(mapper.toResScoreDTO(recordService.saveEmployeeScore(recordId, criteriaId, score, employee)));
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
            @RequestBody Map<String, Object> body) {
        Long criteriaId = Long.valueOf(body.get("criteriaId").toString());
        Double score = Double.valueOf(body.get("score").toString());
        User manager = getCurrentUser();
        return ResponseEntity.ok(mapper.toResScoreDTO(recordService.saveManagerScore(recordId, criteriaId, score, manager)));
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
            @RequestBody EvaluationTrainingPlan plan) {
        User manager = getCurrentUser();
        return ResponseEntity.ok(mapper.toResTrainingPlanDTO(recordService.saveTrainingPlan(recordId, plan, manager)));
    }

    // ═══════════════ GIAI ĐOẠN 3: QUẢN LÝ GIÁN TIẾP PHÊ DUYỆT ═══════════════

    @PostMapping("/records/{recordId}/approver-scores")
    @ApiMessage("Người phê duyệt chấm điểm tiêu chí")
    public ResponseEntity<ResEvaluationRecordDTO.ResScoreDTO> saveApproverScore(
            @PathVariable Long recordId,
            @RequestBody Map<String, Object> body) {
        Long criteriaId = Long.valueOf(body.get("criteriaId").toString());
        Double score = Double.valueOf(body.get("score").toString());
        User approver = getCurrentUser();
        return ResponseEntity.ok(mapper.toResScoreDTO(recordService.saveApproverScore(recordId, criteriaId, score, approver)));
    }

    @PostMapping("/records/{recordId}/approve")
    @ApiMessage("Phê duyệt bản đánh giá")
    public ResponseEntity<ResEvaluationRecordDTO> approveRecord(@PathVariable Long recordId) {
        User approver = getCurrentUser();
        return ResponseEntity.ok(mapper.toResEvaluationRecordDTO(recordService.approveRecord(recordId, approver)));
    }

    @PostMapping("/records/batch-approve")
    @ApiMessage("Phê duyệt hàng loạt")
    public ResponseEntity<List<ResEvaluationRecordDTO>> batchApproveRecords(@RequestBody List<Long> recordIds) {
        User approver = getCurrentUser();
        List<EvaluationRecord> approved = recordService.batchApproveRecords(recordIds, approver);
        return ResponseEntity.ok(approved.stream().map(mapper::toResEvaluationRecordDTO).toList());
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
        return ResponseEntity
                .ok(recordService.fetchHistory(recordId).stream().map(mapper::toResEvaluationHistoryDTO).toList());
    }

    @GetMapping("/records/{recordId}/rejection-count")
    @ApiMessage("Số lần bị trả lại")
    public ResponseEntity<Map<String, Long>> countRejections(@PathVariable Long recordId) {
        long count = recordService.countRejections(recordId);
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
