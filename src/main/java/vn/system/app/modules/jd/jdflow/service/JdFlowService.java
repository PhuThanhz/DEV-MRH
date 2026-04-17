package vn.system.app.modules.jd.jdflow.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.UserScopeContext;
import vn.system.app.modules.jd.jdflow.domain.JdFlow;
import vn.system.app.modules.jd.jdflow.domain.response.ResJdApproverDTO;
import vn.system.app.modules.jd.jdflow.domain.response.ResJdFlowDTO;
import vn.system.app.modules.jd.jdflow.domain.response.ResJdInboxDTO;
import vn.system.app.modules.jd.jdflow.repository.JdFlowRepository;
import vn.system.app.modules.jd.jobdescription.domain.JobDescription;
import vn.system.app.modules.jd.jobdescription.repository.JobDescriptionRepository;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.userposition.repository.UserPositionRepository;

@Service
public class JdFlowService {

    private final JdFlowRepository jdFlowRepository;
    private final UserRepository userRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final JdFlowLogService logService;
    private final JdPermissionService permissionService;
    private final UserPositionRepository userPositionRepository;

    public JdFlowService(
            JdFlowRepository jdFlowRepository,
            UserRepository userRepository,
            JobDescriptionRepository jobDescriptionRepository,
            JdFlowLogService logService,
            JdPermissionService permissionService,
            UserPositionRepository userPositionRepository) {

        this.jdFlowRepository = jdFlowRepository;
        this.userRepository = userRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.logService = logService;
        this.permissionService = permissionService;
        this.userPositionRepository = userPositionRepository;
    }

    /*
     * ==========================================
     * FETCH FLOW BY JD
     * ==========================================
     */
    public ResJdFlowDTO fetchFlowByJd(Long jdId) {
        JdFlow flow = jdFlowRepository.findByJobDescriptionId(jdId);
        if (flow == null)
            return null;
        return convertToDTO(flow);
    }

    /*
     * ==========================================
     * SUBMIT JD / GỬI LẠI DUYỆT
     * Khi bị RETURNED → tự động gửi lại cho người vừa từ chối
     * ==========================================
     */
    @Transactional
    public JdFlow submitFlow(Long jdId, Long nextUserId) {

        JobDescription jd = jobDescriptionRepository.findById(jdId)
                .orElseThrow(() -> new RuntimeException("JD không tồn tại"));

        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        User fromUser = userRepository.findByEmail(email);

        if (fromUser == null)
            throw new RuntimeException("Không xác định được người gửi JD");

        if ("PUBLISHED".equals(jd.getStatus()))
            throw new RuntimeException("JD đã ban hành, không thể gửi duyệt");

        // ================== XỬ LÝ GỬI LẠI SAU RETURNED ==================
        if ("RETURNED".equals(jd.getStatus())) {
            // Lấy người vừa từ chối
            User lastRejector = logService.findLastRejector(jdId);

            if (lastRejector == null) {
                throw new RuntimeException("Không tìm thấy người từ chối gần nhất");
            }

            // Bảo vệ: Không cho phép người vừa từ chối gửi lại cho chính mình
            if (lastRejector.getId().equals(fromUser.getId())) {
                throw new RuntimeException("Bạn không thể gửi lại JD cho chính mình sau khi từ chối");
            }

            // Nếu frontend không truyền nextUserId → tự động gán người vừa từ chối
            if (nextUserId == null) {
                nextUserId = lastRejector.getId();
            }
        }

        if (nextUserId == null)
            throw new RuntimeException("Người duyệt tiếp theo không được để trống");

        User toUser = userRepository.findById(nextUserId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Kiểm tra không được gửi cho chính mình
        if (fromUser.getId().equals(toUser.getId()))
            throw new RuntimeException("Không thể gửi JD duyệt cho chính mình");

        // Chỉ cho phép gửi đến người có quyền DUYỆT — không cho phép gửi thẳng đến
        // issuer
        if (!permissionService.hasApprovePermission(toUser)
                && !permissionService.hasApproveFinalPermission(toUser))
            throw new RuntimeException("Người nhận không có quyền duyệt JD");

        // Reset status khi gửi lại từ REJECTED hoặc RETURNED
        if ("REJECTED".equals(jd.getStatus()) || "RETURNED".equals(jd.getStatus()))
            jd.setStatus("DRAFT");

        JdFlow flow = jdFlowRepository.findByJobDescriptionId(jdId);
        if (flow == null) {
            flow = new JdFlow();
            flow.setJobDescription(jd);
        }

        flow.setFromUser(fromUser);
        flow.setCurrentUser(toUser);
        flow.setStatus("IN_REVIEW");
        jd.setStatus("IN_REVIEW");

        jobDescriptionRepository.save(jd);
        jdFlowRepository.save(flow);

        if (permissionService.hasApproveFinalPermission(toUser)) {
            logService.saveLog(jd, fromUser, toUser, "SUBMIT_TO_FINAL", null);
        } else {
            logService.saveLog(jd, fromUser, toUser, "SUBMIT", null);
        }

        return flow;
    }

    /*
     * ==========================================
     * APPROVE JD
     * ==========================================
     */
    @Transactional
    public JdFlow approveFlow(Long jdId, Long nextUserId) {

        JdFlow flow = jdFlowRepository.findByJobDescriptionId(jdId);

        if (flow == null)
            throw new RuntimeException("JD Flow không tồn tại");

        if ("REJECTED".equals(flow.getStatus()))
            throw new RuntimeException("JD đã bị từ chối");

        if ("ISSUED".equals(flow.getStatus()))
            throw new RuntimeException("JD đã ban hành");

        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        User fromUser = userRepository.findByEmail(email);

        if (fromUser == null || !fromUser.getId().equals(flow.getCurrentUser().getId()))
            throw new RuntimeException("Bạn không phải người đang duyệt JD này");

        // ======= DUYỆT CUỐI (APPROVE_FINAL) =======
        if (permissionService.hasApproveFinalPermission(fromUser)) {

            if (nextUserId == null)
                throw new RuntimeException("Phải chọn người ban hành JD");

            User issuer = userRepository.findById(nextUserId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            if (!permissionService.hasIssuePermission(issuer))
                throw new RuntimeException("User không có quyền ban hành JD");

            // Không cho phép tự chỉ định mình làm người ban hành
            if (fromUser.getId().equals(issuer.getId()))
                throw new RuntimeException("Không thể tự chỉ định mình là người ban hành");

            JobDescription jd = flow.getJobDescription();
            jd.setStatus("APPROVED");

            flow.setStatus("APPROVED");
            flow.setFromUser(fromUser);
            flow.setCurrentUser(issuer);

            jobDescriptionRepository.save(jd);
            jdFlowRepository.save(flow);

            logService.saveLog(jd, fromUser, issuer, "APPROVE_FINAL", null);

            return flow;
        }

        // ======= DUYỆT TRUNG GIAN (APPROVE) =======
        if (nextUserId == null)
            throw new RuntimeException("Phải chọn người duyệt tiếp theo");

        User toUser = userRepository.findById(nextUserId)
                .orElseThrow(() -> new RuntimeException("User tiếp theo không tồn tại"));

        if (fromUser.getId().equals(toUser.getId()))
            throw new RuntimeException("Không thể chuyển duyệt cho chính mình");

        if (!permissionService.hasApprovePermission(toUser)
                && !permissionService.hasApproveFinalPermission(toUser))
            throw new RuntimeException("User tiếp theo không có quyền duyệt JD");

        JobDescription jd = flow.getJobDescription();
        // FIX: cập nhật jd.status khi approve trung gian — trước đây bị thiếu
        jd.setStatus("IN_REVIEW");

        flow.setFromUser(fromUser);
        flow.setCurrentUser(toUser);
        flow.setStatus("IN_REVIEW");

        jobDescriptionRepository.save(jd);
        jdFlowRepository.save(flow);

        logService.saveLog(jd, fromUser, toUser, "APPROVE", null);

        return flow;
    }

    /*
     * ==========================================
     * REJECT JD
     * - Người đang cầm JD (IN_REVIEW hoặc RETURNED) có thể từ chối
     * - Nếu còn approver trước đó → RETURNED về họ
     * - Nếu không còn → REJECTED về người tạo
     * ==========================================
     */
    @Transactional
    public JdFlow rejectFlow(Long jdId, String comment) {

        JdFlow flow = jdFlowRepository.findByJobDescriptionId(jdId);

        if (flow == null)
            throw new RuntimeException("JD Flow không tồn tại");

        // FIX: cho phép reject cả khi RETURNED (approver trung gian muốn trả ngược về
        // trước)
        // Chỉ chặn 2 trạng thái cuối không thể thay đổi
        if ("ISSUED".equals(flow.getStatus()) || "REJECTED".equals(flow.getStatus()))
            throw new RuntimeException("JD không thể từ chối ở trạng thái này");

        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        User rejectUser = userRepository.findByEmail(email);

        if (rejectUser == null || !rejectUser.getId().equals(flow.getCurrentUser().getId()))
            throw new RuntimeException("Bạn không phải người đang xử lý JD này");
        // THÊM VÀO ĐÂY 👇
        User submitUser = logService.findSubmitUser(jdId);
        if (submitUser != null && rejectUser.getId().equals(submitUser.getId()))
            throw new RuntimeException("Người tạo JD không có quyền từ chối");

        JobDescription jd = flow.getJobDescription();

        User previousUser = logService.findPreviousApprover(jdId, rejectUser.getId());

        if (previousUser != null) {
            // Trả về approver trước đó → RETURNED
            flow.setCurrentUser(previousUser);
            flow.setStatus("RETURNED");
            jd.setStatus("RETURNED");
        } else {
            // Không còn approver trước → trả về người tạo, REJECTED
            flow.setCurrentUser(flow.getFromUser());
            flow.setStatus("REJECTED");
            jd.setStatus("REJECTED");
        }

        jobDescriptionRepository.save(jd);
        jdFlowRepository.save(flow);

        logService.saveLog(jd, rejectUser, flow.getCurrentUser(), "REJECT", comment);

        return flow;
    }

    /*
     * ==========================================
     * RECALL JD — Thu hồi JD đang chờ duyệt
     * Chỉ người vừa gửi mới được thu hồi, và JD phải đang IN_REVIEW
     * ==========================================
     */
    @Transactional
    public JdFlow recallFlow(Long jdId) {

        JdFlow flow = jdFlowRepository.findByJobDescriptionId(jdId);

        if (flow == null)
            throw new RuntimeException("JD Flow không tồn tại");

        if (!"IN_REVIEW".equals(flow.getStatus()))
            throw new RuntimeException("Chỉ có thể thu hồi khi JD đang chờ duyệt");

        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        User caller = userRepository.findByEmail(email);

        if (caller == null)
            throw new RuntimeException("Không xác định được người dùng hiện tại");

        // Chỉ người vừa gửi gần nhất mới được thu hồi
        User lastSender = logService.findLastSender(jdId);
        if (lastSender == null || !lastSender.getId().equals(caller.getId()))
            throw new RuntimeException("Bạn không phải người vừa gửi JD này, không thể thu hồi");

        JobDescription jd = flow.getJobDescription();

        flow.setCurrentUser(caller);
        flow.setStatus("RETURNED");
        jd.setStatus("RETURNED");

        jobDescriptionRepository.save(jd);
        jdFlowRepository.save(flow);

        logService.saveLog(jd, caller, caller, "RECALL", null);

        return flow;
    }

    /*
     * ==========================================
     * ISSUE JD
     * ==========================================
     */
    @Transactional
    public JdFlow issueFlow(Long jdId) {

        JdFlow flow = jdFlowRepository.findByJobDescriptionId(jdId);

        if (flow == null)
            throw new RuntimeException("JD Flow không tồn tại");

        if (!"APPROVED".equals(flow.getStatus()))
            throw new RuntimeException("JD chưa được duyệt hoàn tất");

        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        User fromUser = userRepository.findByEmail(email);

        if (fromUser == null || !fromUser.getId().equals(flow.getCurrentUser().getId()))
            throw new RuntimeException("Bạn không phải người ban hành JD này");

        if (!permissionService.hasIssuePermission(fromUser))
            throw new RuntimeException("Bạn không có quyền ban hành JD");

        flow.setStatus("ISSUED");
        flow.setCurrentUser(null);

        JobDescription jd = flow.getJobDescription();
        jd.setStatus("PUBLISHED");

        if (jd.getVersion() == null) {
            jd.setVersion(1);
        } else {
            jd.setVersion(jd.getVersion() + 1);
        }

        jobDescriptionRepository.save(jd);
        jdFlowRepository.save(flow);

        logService.saveLog(jd, fromUser, null, "ISSUE", null);

        return flow;
    }

    /*
     * ==========================================
     * JD CHỜ TÔI XỬ LÝ (INBOX)
     * FIX: thêm REJECTED để người tạo thấy JD bị từ chối trong inbox
     * ==========================================
     */
    public List<ResJdInboxDTO> fetchInbox() {

        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        User user = userRepository.findByEmail(email);

        if (user == null)
            return List.of();

        List<JdFlow> flows = jdFlowRepository
                .findByCurrentUserIdAndStatusIn(
                        user.getId(),
                        // FIX: thêm REJECTED — người tạo JD bị từ chối cuối cùng cũng thấy trong inbox
                        List.of("IN_REVIEW", "APPROVED", "RETURNED", "REJECTED"));

        return flows.stream().map(flow -> {

            JobDescription jd = flow.getJobDescription();
            ResJdInboxDTO dto = new ResJdInboxDTO();

            dto.setJdId(jd.getId());
            dto.setCode(jd.getCode());
            dto.setStatus(jd.getStatus());
            dto.setUpdatedAt(flow.getUpdatedAt() != null ? flow.getUpdatedAt() : flow.getCreatedAt());

            if (jd.getCompany() != null)
                dto.setCompanyName(jd.getCompany().getName());

            if (jd.getDepartment() != null)
                dto.setDepartmentName(jd.getDepartment().getName());

            if (jd.getCompanyJobTitle() != null)
                dto.setJobTitleName(jd.getCompanyJobTitle().getJobTitle().getNameVi());

            if (flow.getCurrentUser() != null) {
                ResJdInboxDTO.UserSimple current = new ResJdInboxDTO.UserSimple();
                current.setId(flow.getCurrentUser().getId());
                current.setName(flow.getCurrentUser().getName());
                dto.setCurrentUser(current);
            }

            User fromUser = logService.findLastSender(jd.getId());
            if (fromUser != null) {
                ResJdInboxDTO.UserSimple from = new ResJdInboxDTO.UserSimple();
                from.setId(fromUser.getId());
                from.setName(fromUser.getName());
                dto.setFromUser(from);
            }

            if ("REJECTED".equals(jd.getStatus()) || "RETURNED".equals(jd.getStatus())) {
                JdFlowLogService.RejectInfo rejectInfo = logService.findLatestRejectInfo(jd.getId());
                if (rejectInfo != null) {
                    dto.setRejectComment(rejectInfo.getComment());
                    dto.setRejectorName(rejectInfo.getRejectorName());
                    dto.setRejectorPosition(rejectInfo.getRejectorPosition());
                    dto.setRejectorDepartment(rejectInfo.getRejectorDepartment());
                    dto.setRejectorPositionCode(rejectInfo.getRejectorPositionCode());
                }
            }

            return dto;

        }).collect(Collectors.toList());
    }

    // ====================== HELPER METHODS ======================

    private boolean isUserInScope(User u, UserScopeContext.UserScope scope) {
        if (scope == null || scope.isSuperAdmin())
            return true;
        return userPositionRepository.findByUser_IdAndActiveTrue(u.getId())
                .stream()
                .anyMatch(pos -> {
                    Long companyId = switch (pos.getSource().toUpperCase()) {
                        case "COMPANY" -> pos.getCompanyJobTitle().getCompany().getId();
                        case "DEPARTMENT" -> pos.getDepartmentJobTitle().getDepartment().getCompany().getId();
                        case "SECTION" -> pos.getSectionJobTitle().getSection().getDepartment().getCompany().getId();
                        default -> null;
                    };
                    return companyId != null && scope.companyIds().contains(companyId);
                });
    }

    private List<ResJdApproverDTO.PositionInfo> buildPositions(Long userId) {
        return userPositionRepository.findByUser_IdAndActiveTrue(userId)
                .stream()
                .map(pos -> {
                    ResJdApproverDTO.PositionInfo info = new ResJdApproverDTO.PositionInfo();
                    info.setSource(pos.getSource());

                    switch (pos.getSource().toUpperCase()) {
                        case "COMPANY" -> {
                            var cjt = pos.getCompanyJobTitle();
                            info.setCompanyName(cjt.getCompany().getName());
                            info.setJobTitleName(cjt.getJobTitle().getNameVi());
                            if (cjt.getJobTitle().getPositionLevel() != null)
                                info.setPositionCode(cjt.getJobTitle().getPositionLevel().getCode());
                        }
                        case "DEPARTMENT" -> {
                            var djt = pos.getDepartmentJobTitle();
                            info.setCompanyName(djt.getDepartment().getCompany().getName());
                            info.setDepartmentName(djt.getDepartment().getName());
                            info.setJobTitleName(djt.getJobTitle().getNameVi());
                            if (djt.getJobTitle().getPositionLevel() != null)
                                info.setPositionCode(djt.getJobTitle().getPositionLevel().getCode());
                        }
                        case "SECTION" -> {
                            var sjt = pos.getSectionJobTitle();
                            info.setCompanyName(sjt.getSection().getDepartment().getCompany().getName());
                            info.setDepartmentName(sjt.getSection().getDepartment().getName());
                            info.setJobTitleName(sjt.getJobTitle().getNameVi());
                            if (sjt.getJobTitle().getPositionLevel() != null)
                                info.setPositionCode(sjt.getJobTitle().getPositionLevel().getCode());
                        }
                    }
                    return info;
                })
                .collect(Collectors.toList());
    }

    public List<ResJdApproverDTO> fetchApprovers() {
        UserScopeContext.UserScope scope = UserScopeContext.get();

        return userRepository.findAll().stream()
                .filter(u -> isUserInScope(u, scope)
                        && (permissionService.hasApprovePermission(u)
                                || permissionService.hasApproveFinalPermission(u)))
                .map(u -> {
                    ResJdApproverDTO dto = new ResJdApproverDTO(
                            u.getId(),
                            u.getName(),
                            u.getEmail(),
                            u.getAvatar(),
                            permissionService.hasApproveFinalPermission(u));
                    dto.setPositions(buildPositions(u.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ResJdApproverDTO> fetchIssuers() {
        UserScopeContext.UserScope scope = UserScopeContext.get();

        return userRepository.findAll().stream()
                .filter(u -> isUserInScope(u, scope)
                        && permissionService.hasIssuePermission(u))
                .map(u -> {
                    ResJdApproverDTO dto = new ResJdApproverDTO(
                            u.getId(),
                            u.getName(),
                            u.getEmail(),
                            u.getAvatar(),
                            false);
                    dto.setPositions(buildPositions(u.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private ResJdFlowDTO convertToDTO(JdFlow flow) {
        ResJdFlowDTO dto = new ResJdFlowDTO();

        dto.setJdId(flow.getJobDescription().getId());
        dto.setCode(flow.getJobDescription().getCode());
        dto.setStatus(flow.getJobDescription().getStatus());
        dto.setUpdatedAt(flow.getUpdatedAt() != null ? flow.getUpdatedAt() : flow.getCreatedAt());

        if (flow.getCurrentUser() != null) {
            dto.setCurrentUser(new ResJdFlowDTO.UserInfo(
                    flow.getCurrentUser().getId(),
                    flow.getCurrentUser().getName()));
            dto.setCurrentUserIsFinal(
                    permissionService.hasApproveFinalPermission(flow.getCurrentUser()));
        }

        User fromUser = logService.findLastSender(flow.getJobDescription().getId());
        if (fromUser != null) {
            dto.setFromUser(new ResJdFlowDTO.UserInfo(
                    fromUser.getId(),
                    fromUser.getName()));
        }

        return dto;
    }
}