package vn.system.app.modules.jdflow.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.jd.jobdescription.domain.JDStatus;
import vn.system.app.modules.jd.jobdescription.domain.JobDescription;
import vn.system.app.modules.jd.jobdescription.repository.JobDescriptionRepository;
import vn.system.app.modules.jdflow.domain.JobDescriptionFlow;
import vn.system.app.modules.jdflow.domain.JobDescriptionFlow.FlowStatus;
import vn.system.app.modules.jdflow.domain.request.ReqCreateFlow;
import vn.system.app.modules.jdflow.domain.request.ReqRejectFlow;
import vn.system.app.modules.jdflow.domain.response.ResApproverDTO;
import vn.system.app.modules.jdflow.domain.response.ResJobDescriptionFlowDTO;
import vn.system.app.modules.jdflow.repository.JobDescriptionFlowRepository;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;

@Service
public class JobDescriptionFlowService {

    private final JobDescriptionFlowRepository flowRepo;
    private final JobDescriptionRepository jdRepo;
    private final UserRepository userRepo;
    private final JobDescriptionFlowQueryService queryService;
    private final JobDescriptionFlowActionService actionService;
    private final JobDescriptionFlowPermissionService permissionService;

    public JobDescriptionFlowService(
            JobDescriptionFlowRepository flowRepo,
            JobDescriptionRepository jdRepo,
            UserRepository userRepo,
            JobDescriptionFlowQueryService queryService,
            JobDescriptionFlowActionService actionService,
            JobDescriptionFlowPermissionService permissionService) {

        this.flowRepo = flowRepo;
        this.jdRepo = jdRepo;
        this.userRepo = userRepo;
        this.queryService = queryService;
        this.actionService = actionService;
        this.permissionService = permissionService;
    }

    // =====================================================
    // CREATE FLOW – GỬI JD ĐI DUYỆT
    // =====================================================
    public ResJobDescriptionFlowDTO createFlow(ReqCreateFlow req)
            throws IdInvalidException {

        if (req == null || req.getJobDescriptionId() == null || req.getToUserId() == null) {
            throw new IdInvalidException("Thiếu dữ liệu bắt buộc");
        }

        JobDescription jd = jdRepo.findById(req.getJobDescriptionId())
                .orElseThrow(() -> new IdInvalidException("JD không tồn tại"));

        if (jd.getStatus() != JDStatus.DRAFT) {
            throw new IdInvalidException("Chỉ JD ở trạng thái DRAFT mới được gửi duyệt");
        }

        if (flowRepo.existsByJobDescriptionIdAndStatus(jd.getId(), FlowStatus.PENDING)) {
            throw new IdInvalidException("JD đang trong quá trình duyệt");
        }

        User actor = getCurrentUser();

        User approver = userRepo.findById(req.getToUserId())
                .orElseThrow(() -> new IdInvalidException("Người duyệt không tồn tại"));

        // Validate nghiệp vụ
        permissionService.validateSubmit(actor, approver);

        JobDescriptionFlow flow = new JobDescriptionFlow();
        flow.setJobDescriptionId(jd.getId());
        flow.setFromUserId(actor.getId());
        flow.setToUserId(approver.getId());
        flow.setStatus(FlowStatus.PENDING);

        JobDescriptionFlow saved = flowRepo.save(flow);

        // JD chuyển sang trạng thái chờ duyệt
        jd.setStatus(JDStatus.PENDING);
        jdRepo.save(jd);

        return toRes(saved);
    }

    // =====================================================
    // APPROVE – DUYỆT & CHUYỂN CẤP
    // =====================================================
    public ResJobDescriptionFlowDTO approve(Long flowId, Long nextUserId)
            throws IdInvalidException {

        JobDescriptionFlow flow = queryService.getPendingFlow(flowId);
        User actor = getCurrentUser();

        return toRes(actionService.approve(flow, actor, nextUserId));
    }

    // =====================================================
    // REJECT – TỪ CHỐI
    // =====================================================
    public ResJobDescriptionFlowDTO reject(Long flowId, ReqRejectFlow req)
            throws IdInvalidException {

        if (req == null || req.getReason() == null || req.getReason().isBlank()) {
            throw new IdInvalidException("Thiếu lý do từ chối");
        }

        JobDescriptionFlow flow = queryService.getPendingFlow(flowId);
        User actor = getCurrentUser();

        return toRes(actionService.reject(flow, actor, req.getReason()));
    }

    // =====================================================
    // ISSUE – BAN HÀNH JD
    // =====================================================
    public ResJobDescriptionFlowDTO issue(Long flowId)
            throws IdInvalidException {

        JobDescriptionFlow flow = queryService.getWaitingIssueFlow(flowId);
        User actor = getCurrentUser();

        return toRes(actionService.issue(flow, actor));
    }

    // =====================================================
    // LẤY DANH SÁCH NGƯỜI DUYỆT CAO CẤP HƠN USER HIỆN TẠI
    // =====================================================
    public List<ResApproverDTO> getApproversHigherThanCurrentUser()
            throws IdInvalidException {

        User actor = getCurrentUser();

        return permissionService.getApproversHigherThan(actor)
                .stream()
                .map(u -> new ResApproverDTO(
                        u.getId(),
                        u.getName(),
                        u.getEmail()))
                .collect(Collectors.toList());
    }

    // =====================================================
    // GET CURRENT USER
    // =====================================================
    private User getCurrentUser() throws IdInvalidException {

        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Không xác định user đăng nhập"));

        User user = userRepo.findByEmail(email);

        if (user == null) {
            throw new IdInvalidException("User không tồn tại");
        }

        return user;
    }

    // =====================================================
    // ENTITY → DTO
    // =====================================================
    private ResJobDescriptionFlowDTO toRes(JobDescriptionFlow flow) {

        ResJobDescriptionFlowDTO dto = new ResJobDescriptionFlowDTO();

        dto.setId(flow.getId());
        dto.setJobDescriptionId(flow.getJobDescriptionId());
        dto.setFromUserId(flow.getFromUserId());
        dto.setToUserId(flow.getToUserId());
        dto.setStatus(flow.getStatus().name());
        dto.setCreatedAt(flow.getCreatedAt());

        // ❌ Không còn updatedAt → xóa

        return dto;
    }
}