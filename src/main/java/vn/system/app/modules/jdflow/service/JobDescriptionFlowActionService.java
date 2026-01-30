package vn.system.app.modules.jdflow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.jd.domain.JobDescription;
import vn.system.app.modules.jd.repository.JobDescriptionRepository;
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

    /*
     * =====================================================
     * APPROVE (Duyệt & chuyển cấp)
     * =====================================================
     */
    public JobDescriptionFlow approve(
            JobDescriptionFlow flow,
            User actor,
            Long nextUserId)
            throws IdInvalidException {

        if (flow.getStatus() != FlowStatus.PENDING) {
            throw new IdInvalidException("Flow không ở trạng thái duyệt");
        }

        if (!Long.valueOf(actor.getId()).equals(flow.getToUserId())) {
            throw new IdInvalidException("Bạn không phải người đang được giao duyệt");
        }

        permissionService.checkActorApprovePermission(actor);

        JobDescription jd = jdRepo.findById(flow.getJobDescriptionId())
                .orElseThrow(() -> new IdInvalidException("JD không tồn tại"));
        jd.setStatus("PROCESSING");
        jdRepo.save(jd);

        /*
         * ========== KẾT THÚC DUYỆT ==========
         */
        if (nextUserId == null) {

            flow.setStatus(FlowStatus.WAITING_ISSUE);
            flow.setFromUserId(actor.getId());
            flow.setToUserId(actor.getId());

            JobDescriptionFlow saved = flowRepo.save(flow);

            logService.log(
                    flow.getJobDescriptionId(),
                    flow.getId(),
                    "APPROVE_END",
                    actor.getId(),
                    null,
                    "Kết thúc duyệt, chờ ban hành");

            return saved;
        }

        /*
         * ========== CHUYỂN TIẾP DUYỆT ==========
         */
        if (Long.valueOf(nextUserId).equals(actor.getId())) {
            throw new IdInvalidException("Không thể giao duyệt cho chính mình");
        }

        User nextUser = userRepo.findById(nextUserId)
                .orElseThrow(() -> new IdInvalidException("Người duyệt tiếp theo không tồn tại"));

        permissionService.validateNextApprover(actor, nextUser);

        flow.setFromUserId(actor.getId());
        flow.setToUserId(nextUser.getId());
        flow.setStatus(FlowStatus.PENDING);

        JobDescriptionFlow saved = flowRepo.save(flow);

        logService.log(
                flow.getJobDescriptionId(),
                flow.getId(),
                "APPROVE",
                actor.getId(),
                nextUser.getId(),
                null);

        return saved;
    }

    /*
     * =====================================================
     * REJECT
     * =====================================================
     */
    public JobDescriptionFlow reject(
            JobDescriptionFlow flow,
            User actor,
            String comment)
            throws IdInvalidException {

        if (comment == null || comment.isBlank()) {
            throw new IdInvalidException("Bắt buộc nhập lý do từ chối");
        }

        if (flow.getStatus() != FlowStatus.PENDING) {
            throw new IdInvalidException("Flow không ở trạng thái duyệt");
        }

        if (!Long.valueOf(actor.getId()).equals(flow.getToUserId())) {
            throw new IdInvalidException("Bạn không phải người đang được giao duyệt");
        }

        permissionService.checkActorApprovePermission(actor);

        JobDescription jd = jdRepo.findById(flow.getJobDescriptionId())
                .orElseThrow(() -> new IdInvalidException("JD không tồn tại"));
        jd.setStatus("DRAFT");
        jdRepo.save(jd);

        Long previousUserId = flow.getFromUserId();

        flow.setStatus(FlowStatus.REJECTED);
        flow.setFromUserId(actor.getId());
        flow.setToUserId(previousUserId);

        JobDescriptionFlow saved = flowRepo.save(flow);

        logService.log(
                flow.getJobDescriptionId(),
                flow.getId(),
                "REJECT",
                actor.getId(),
                previousUserId,
                comment);

        return saved;
    }

    /*
     * =====================================================
     * ISSUE – BAN HÀNH JD
     * =====================================================
     */
    public JobDescriptionFlow issue(
            JobDescriptionFlow flow,
            User actor)
            throws IdInvalidException {

        if (flow.getStatus() != FlowStatus.WAITING_ISSUE) {
            throw new IdInvalidException("JD chưa sẵn sàng để ban hành");
        }

        if (!Long.valueOf(actor.getId()).equals(flow.getToUserId())) {
            throw new IdInvalidException("Bạn không phải người được giao ban hành JD");
        }

        permissionService.checkIssuePermission(actor);

        flow.setStatus(FlowStatus.DONE);
        flow.setFromUserId(actor.getId());
        flow.setToUserId(null);

        JobDescriptionFlow saved = flowRepo.save(flow);

        JobDescription jd = jdRepo.findById(flow.getJobDescriptionId())
                .orElseThrow(() -> new IdInvalidException("JD không tồn tại"));
        jd.setStatus("PUBLIC");
        jdRepo.save(jd);

        logService.log(
                flow.getJobDescriptionId(),
                flow.getId(),
                "ISSUE",
                actor.getId(),
                null,
                "Ban hành JD");

        return saved;
    }
}
