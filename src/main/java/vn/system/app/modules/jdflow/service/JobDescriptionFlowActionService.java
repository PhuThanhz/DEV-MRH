package vn.system.app.modules.jdflow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.jd.jobdescription.domain.JDStatus;
import vn.system.app.modules.jd.jobdescription.domain.JobDescription;
import vn.system.app.modules.jd.jobdescription.repository.JobDescriptionRepository;
import vn.system.app.modules.jdflow.domain.JobDescriptionFlow;
import vn.system.app.modules.jdflow.domain.JobDescriptionFlow.FlowStatus;
import vn.system.app.modules.jdflow.repository.JobDescriptionFlowRepository;
import vn.system.app.modules.logjdflow.service.LogJobDescriptionFlowService;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;

@Service
@Transactional
public class JobDescriptionFlowActionService {

    private final JobDescriptionFlowRepository flowRepo;
    private final JobDescriptionRepository jdRepo;
    private final UserRepository userRepo;
    private final JobDescriptionFlowPermissionService permissionService;
    private final LogJobDescriptionFlowService logService;

    public JobDescriptionFlowActionService(
            JobDescriptionFlowRepository flowRepo,
            JobDescriptionRepository jdRepo,
            UserRepository userRepo,
            JobDescriptionFlowPermissionService permissionService,
            LogJobDescriptionFlowService logService) {

        this.flowRepo = flowRepo;
        this.jdRepo = jdRepo;
        this.userRepo = userRepo;
        this.permissionService = permissionService;
        this.logService = logService;
    }

    // =====================================================================
    // SUBMIT – GỬI JD ĐI DUYỆT LẦN ĐẦU
    // =====================================================================
    public JobDescriptionFlow submit(Long jdId, User actor, Long toUserId)
            throws IdInvalidException {

        JobDescription jd = jdRepo.findById(jdId)
                .orElseThrow(() -> new IdInvalidException("JD không tồn tại"));

        if (jd.getStatus() != JDStatus.DRAFT) {
            throw new IdInvalidException("Chỉ JD trạng thái DRAFT mới được gửi duyệt");
        }

        User nextUser = userRepo.findById(toUserId)
                .orElseThrow(() -> new IdInvalidException("Người duyệt đầu tiên không tồn tại"));

        // Validate nghiệp vụ: actor → nextUser
        permissionService.validateSubmit(actor, nextUser);

        // Tạo dòng flow mới
        JobDescriptionFlow flow = new JobDescriptionFlow();
        flow.setJobDescriptionId(jdId);
        flow.setFromUserId(actor.getId());
        flow.setToUserId(nextUser.getId());
        flow.setStatus(FlowStatus.PENDING);

        JobDescriptionFlow saved = flowRepo.save(flow);

        // JD chuyển sang trạng thái PENDING
        jd.setStatus(JDStatus.PENDING);
        jdRepo.save(jd);

        // Log
        logService.log(jdId, saved.getId(), "SUBMIT",
                actor.getId(), nextUser.getId(),
                "Gửi JD đi duyệt");

        return saved;
    }

    // =====================================================================
    // APPROVE – DUYỆT & CHUYỂN CẤP
    // =====================================================================
    public JobDescriptionFlow approve(JobDescriptionFlow currentFlow, User actor, Long nextUserId)
            throws IdInvalidException {

        if (currentFlow.getStatus() != FlowStatus.PENDING) {
            throw new IdInvalidException("Flow hiện tại không còn ở trạng thái PENDING");
        }

        if (!actor.getId().equals(currentFlow.getToUserId())) {
            throw new IdInvalidException("Bạn không phải người được giao duyệt bước này");
        }

        permissionService.checkApprovePermission(actor);

        // ==============================
        // KẾT THÚC DUYỆT – CHỜ CEO BAN HÀNH
        // ==============================
        if (nextUserId == null) {

            JobDescriptionFlow newFlow = new JobDescriptionFlow();
            newFlow.setJobDescriptionId(currentFlow.getJobDescriptionId());
            newFlow.setFromUserId(actor.getId());
            newFlow.setToUserId(actor.getId()); // CEO sẽ thấy WAITING_ISSUE
            newFlow.setStatus(FlowStatus.WAITING_ISSUE);

            JobDescriptionFlow saved = flowRepo.save(newFlow);

            logService.log(
                    currentFlow.getJobDescriptionId(),
                    saved.getId(),
                    "APPROVE_END",
                    actor.getId(),
                    null,
                    "Kết thúc duyệt, chờ ban hành");

            return saved;
        }

        // ==============================
        // CHUYỂN TIẾP DUYỆT
        // ==============================
        if (actor.getId().equals(nextUserId)) {
            throw new IdInvalidException("Không thể giao duyệt cho chính mình");
        }

        User nextUser = userRepo.findById(nextUserId)
                .orElseThrow(() -> new IdInvalidException("Người duyệt tiếp theo không tồn tại"));

        permissionService.validateApproveAndNext(actor, nextUser);

        JobDescriptionFlow newFlow = new JobDescriptionFlow();
        newFlow.setJobDescriptionId(currentFlow.getJobDescriptionId());
        newFlow.setFromUserId(actor.getId());
        newFlow.setToUserId(nextUser.getId());
        newFlow.setStatus(FlowStatus.PENDING);

        JobDescriptionFlow saved = flowRepo.save(newFlow);

        logService.log(
                currentFlow.getJobDescriptionId(),
                saved.getId(),
                "APPROVE",
                actor.getId(),
                nextUser.getId(),
                null);

        return saved;
    }

    // =====================================================================
    // REJECT – TỪ CHỐI JD VÀ TRẢ VỀ NGƯỜI TRƯỚC
    // =====================================================================
    public JobDescriptionFlow reject(JobDescriptionFlow currentFlow, User actor, String reason)
            throws IdInvalidException {

        if (reason == null || reason.isBlank()) {
            throw new IdInvalidException("Lý do từ chối không được để trống");
        }

        if (currentFlow.getStatus() != FlowStatus.PENDING) {
            throw new IdInvalidException("Flow hiện tại không ở trạng thái PENDING");
        }

        if (!actor.getId().equals(currentFlow.getToUserId())) {
            throw new IdInvalidException("Bạn không phải người được giao duyệt ở bước này");
        }

        permissionService.checkApprovePermission(actor);

        JobDescription jd = jdRepo.findById(currentFlow.getJobDescriptionId())
                .orElseThrow(() -> new IdInvalidException("JD không tồn tại"));

        // Khi bị từ chối → JD về lại trạng thái DRAFT
        jd.setStatus(JDStatus.DRAFT);
        jdRepo.save(jd);

        Long previousUserId = currentFlow.getFromUserId();

        // Tạo flow mới
        JobDescriptionFlow newFlow = new JobDescriptionFlow();
        newFlow.setJobDescriptionId(currentFlow.getJobDescriptionId());
        newFlow.setFromUserId(actor.getId());
        newFlow.setToUserId(previousUserId);
        newFlow.setReason(reason);
        newFlow.setStatus(FlowStatus.REJECTED);

        JobDescriptionFlow saved = flowRepo.save(newFlow);

        logService.log(
                currentFlow.getJobDescriptionId(),
                saved.getId(),
                "REJECT",
                actor.getId(),
                previousUserId,
                reason);

        return saved;
    }

    // =====================================================================
    // ISSUE – BAN HÀNH JD
    // =====================================================================
    public JobDescriptionFlow issue(JobDescriptionFlow currentFlow, User actor)
            throws IdInvalidException {

        if (currentFlow.getStatus() != FlowStatus.WAITING_ISSUE) {
            throw new IdInvalidException("JD chưa ở trạng thái WAITING_ISSUE");
        }

        if (!actor.getId().equals(currentFlow.getToUserId())) {
            throw new IdInvalidException("Bạn không phải người được giao ban hành");
        }

        permissionService.checkIssuePermission(actor);

        // Tạo dòng flow mới
        JobDescriptionFlow newFlow = new JobDescriptionFlow();
        newFlow.setJobDescriptionId(currentFlow.getJobDescriptionId());
        newFlow.setFromUserId(actor.getId());
        newFlow.setToUserId(null);
        newFlow.setStatus(FlowStatus.DONE);

        JobDescriptionFlow saved = flowRepo.save(newFlow);

        // Cập nhật trạng thái JD chính thức
        JobDescription jd = jdRepo.findById(currentFlow.getJobDescriptionId())
                .orElseThrow(() -> new IdInvalidException("JD không tồn tại"));

        jd.setStatus(JDStatus.PUBLISHED);
        jdRepo.save(jd);

        logService.log(
                currentFlow.getJobDescriptionId(),
                saved.getId(),
                "ISSUE",
                actor.getId(),
                null,
                "Ban hành JD");

        return saved;
    }
}