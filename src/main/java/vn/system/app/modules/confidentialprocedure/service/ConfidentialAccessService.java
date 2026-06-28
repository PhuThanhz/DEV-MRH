package vn.system.app.modules.confidentialprocedure.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.confidentialprocedure.domain.ConfidentialProcedure;
import vn.system.app.modules.confidentialprocedure.domain.ConfidentialProcedureAccess;
import vn.system.app.modules.confidentialprocedure.domain.request.ShareRequest;
import vn.system.app.modules.confidentialprocedure.domain.request.ConfidentialProcedureRequest;
import vn.system.app.modules.confidentialprocedure.domain.response.ResAccessDTO;
import vn.system.app.modules.confidentialprocedure.repository.ConfidentialProcedureAccessRepository;
import vn.system.app.modules.confidentialprocedure.repository.ConfidentialProcedureRepository;
import vn.system.app.modules.notification.event.AppNotificationEvent;

@Service
public class ConfidentialAccessService {

    private final ConfidentialProcedureAccessRepository accessRepository;
    private final ConfidentialProcedureRepository procedureRepository;
    private final UserRepository userRepository;
    private final ConfidentialShareLogService shareLogService;
    private final ApplicationEventPublisher eventPublisher;

    public ConfidentialAccessService(
            ConfidentialProcedureAccessRepository accessRepository,
            ConfidentialProcedureRepository procedureRepository,
            UserRepository userRepository,
            ConfidentialShareLogService shareLogService,
            ApplicationEventPublisher eventPublisher) {

        this.accessRepository = accessRepository;
        this.procedureRepository = procedureRepository;
        this.userRepository = userRepository;
        this.shareLogService = shareLogService;
        this.eventPublisher = eventPublisher;
    }

    // =====================================================
    // SHARE
    // =====================================================
    @Transactional
    public void handleShare(Long procedureId, ShareRequest req) {
        ConfidentialProcedure procedure = fetchProcedure(procedureId);
        String currentUserId = getCurrentUserId();

        if (req.getUserIds() == null || req.getUserIds().isEmpty()) {
            throw new IdInvalidException("Danh sách user không được để trống");
        }

        // Lấy tên người gửi để hiển thị trong thông báo
        String senderName = userRepository.findById(currentUserId)
                .map(User::getName)
                .orElse("Hệ thống");

        List<String> newlySharedUserIds = new ArrayList<>();

        for (String userId : req.getUserIds()) {
            userRepository.findById(userId)
                    .orElseThrow(() -> new IdInvalidException("User không tồn tại: " + userId));

            if (accessRepository.findByProcedure_IdAndUserIdAndAccessType(procedureId, userId, "USER").isPresent()) {
                continue;
            }

            ConfidentialProcedureAccess access = new ConfidentialProcedureAccess();
            access.setProcedure(procedure);
            access.setUserId(userId);
            access.setAccessType("USER");
            access.setAssignedBy(currentUserId);
            access.setAssignedAt(Instant.now());
            accessRepository.save(access);

            shareLogService.saveShareLog(procedureId, currentUserId, userId, "SHARE");
            newlySharedUserIds.add(userId);
        }

        // Gửi thông báo tới tất cả người được chia sẻ
        if (!newlySharedUserIds.isEmpty()) {
            String content = String.format(
                    "Quy trình bảo mật: %s (%s) vừa được chia sẻ với bạn bởi %s.",
                    procedure.getProcedureName(), procedure.getProcedureCode(), senderName);
            String actionLink = "/admin/procedures/confidential?viewId=" + procedureId;

            eventPublisher.publishEvent(new AppNotificationEvent(
                    newlySharedUserIds,
                    "COMPANY_PROCEDURES",
                    "CONFIDENTIAL_PROCEDURE_SHARED",
                    content,
                    actionLink
            ));
        }
    }

    // =====================================================
    // SAVE ACCESS LIST (dùng khi create/update/revise)
    // logShare = true chỉ khi CREATE → ghi log + gửi notification
    // logShare = false khi UPDATE/REVISE → không ghi log, không notify
    // =====================================================
    @Transactional
    public void saveAccessList(ConfidentialProcedure procedure,
            ConfidentialProcedureRequest req,
            boolean logShare) {

        String currentUserId = getCurrentUserId();

        // Lấy tên người tạo để hiển thị trong thông báo
        String senderName = userRepository.findById(currentUserId)
                .map(User::getName)
                .orElse("Hệ thống");

        List<String> notifyUserIds = new ArrayList<>();

        if (req.getUserIds() != null) {
            req.getUserIds().forEach(userId -> {
                accessRepository.deleteByProcedure_IdAndUserIdAndAccessType(
                        procedure.getId(), userId, "USER");

                ConfidentialProcedureAccess access = new ConfidentialProcedureAccess();
                access.setProcedure(procedure);
                access.setUserId(userId);
                access.setAccessType("USER");
                access.setAssignedBy(currentUserId);
                access.setAssignedAt(Instant.now());
                accessRepository.save(access);

                if (logShare) {
                    shareLogService.saveShareLog(procedure.getId(), currentUserId, userId, "SHARE");
                    notifyUserIds.add(userId);
                }
            });
        }

        if (req.getRoleIds() != null) {
            req.getRoleIds().forEach(roleId -> {
                ConfidentialProcedureAccess access = new ConfidentialProcedureAccess();
                access.setProcedure(procedure);
                access.setRoleId(roleId);
                access.setAccessType("ROLE");
                accessRepository.save(access);
            });
        }

        // Gửi notification khi CREATE (logShare = true) — frontend không gọi /share sau create
        if (logShare && !notifyUserIds.isEmpty()) {
            String content = String.format(
                    "Quy trình bảo mật: %s (%s) vừa được chia sẻ với bạn bởi %s.",
                    procedure.getProcedureName(), procedure.getProcedureCode(), senderName);
            String actionLink = "/admin/procedures/confidential?viewId=" + procedure.getId();

            eventPublisher.publishEvent(new AppNotificationEvent(
                    notifyUserIds,
                    "COMPANY_PROCEDURES",
                    "CONFIDENTIAL_PROCEDURE_SHARED",
                    content,
                    actionLink
            ));
        }
    }

    // =====================================================
    // ACCESS LIST
    // =====================================================
    public List<ResAccessDTO> handleGetAccessList(Long procedureId) {
        fetchProcedure(procedureId);

        return accessRepository.findByProcedure_Id(procedureId)
                .stream()
                .filter(a -> "USER".equals(a.getAccessType()))
                .map(a -> {
                    ResAccessDTO dto = new ResAccessDTO();
                    dto.setUserId(a.getUserId());
                    dto.setAssignedAt(a.getAssignedAt());

                    userRepository.findById(a.getUserId()).ifPresent(u -> {
                        dto.setName(u.getName());
                        dto.setEmail(u.getEmail());
                    });

                    if (a.getAssignedBy() != null) {
                        userRepository.findById(a.getAssignedBy())
                                .ifPresent(u -> dto.setAssignedByName(u.getName()));
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // =====================================================
    // REVOKE
    // Chỉ creator hoặc SUPER_ADMIN / ADMIN_SUB_1 mới được revoke
    // =====================================================
    @Transactional
    public void handleRevoke(Long procedureId, String userId) {
        ConfidentialProcedure procedure = fetchProcedure(procedureId);

        String currentEmail = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Không xác định user"));
        User currentUser = userRepository.findByEmail(currentEmail);
        if (currentUser == null) {
            throw new IdInvalidException("User không tồn tại");
        }

        boolean isAdmin = currentUser.getRole() != null &&
                ("SUPER_ADMIN".equals(currentUser.getRole().getName()) ||
                        "ADMIN_SUB_1".equals(currentUser.getRole().getName()));
        boolean isCreator = currentEmail.equals(procedure.getCreatedBy());

        if (!isAdmin && !isCreator) {
            throw new IdInvalidException("Bạn không có quyền thu hồi truy cập này");
        }

        ConfidentialProcedureAccess target = accessRepository
                .findByProcedure_IdAndUserIdAndAccessType(procedureId, userId, "USER")
                .orElseThrow(() -> new IdInvalidException("User này không có quyền truy cập"));

        accessRepository.delete(target);
        shareLogService.saveShareLog(procedureId, currentUser.getId(), userId, "REVOKE");
    }

    // =====================================================
    // CHECK ACCESS (dùng ở controller getOne / getHistory)
    // =====================================================
    public void checkAccess(Long procedureId) {
        String currentEmail = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Không xác định được người dùng"));

        User user = userRepository.findByEmail(currentEmail);
        if (user == null) {
            throw new IdInvalidException("Người dùng không tồn tại");
        }

        if (user.getRole() != null) {
            String roleName = user.getRole().getName();
            if ("SUPER_ADMIN".equals(roleName) || "ADMIN_SUB_1".equals(roleName)) {
                return;
            }
        }

        ConfidentialProcedure procedure = fetchProcedure(procedureId);

        if (currentEmail.equals(procedure.getCreatedBy())) {
            return;
        }

        List<ConfidentialProcedureAccess> accessList = accessRepository.findByProcedure_Id(procedureId);
        boolean allowed = accessList.stream()
                .anyMatch(a -> "USER".equals(a.getAccessType()) && user.getId().equals(a.getUserId()));

        if (!allowed) {
            throw new IdInvalidException("Bạn không có quyền truy cập quy trình bảo mật này");
        }
    }

    // =====================================================
    // HAS ACCESS (dùng nội bộ nếu cần)
    // =====================================================
    public boolean hasAccess(Long procedureId, String userId, List<Long> userRoleIds) {
        List<ConfidentialProcedureAccess> accessList = accessRepository.findByProcedure_Id(procedureId);

        return accessList.stream().anyMatch(a -> {
            if ("USER".equals(a.getAccessType())) {
                return userId.equals(a.getUserId());
            }
            if ("ROLE".equals(a.getAccessType())) {
                return userRoleIds != null && userRoleIds.contains(a.getRoleId());
            }
            return false;
        });
    }

    // =====================================================
    // SYNC ACCESS LIST (UPDATE / REVISE)
    // =====================================================
    @Transactional
    public void syncAccessList(ConfidentialProcedure procedure, List<String> newUserIds) {
        String currentUserId = getCurrentUserId();
        Long procedureId = procedure.getId();

        // ✅ SỬA THÀNH
        List<ConfidentialProcedureAccess> existingAccesses = accessRepository
                .findByProcedure_Id(procedureId)
                .stream()
                .filter(a -> "USER".equals(a.getAccessType()))
                .collect(Collectors.toList());

        Set<String> existingSet = existingAccesses.stream()
                .map(ConfidentialProcedureAccess::getUserId)
                .collect(Collectors.toCollection(HashSet::new));
        Set<String> newSet = new HashSet<>(newUserIds); // ← thêm dòng này

        // SHARE
        for (String userId : newUserIds) {
            if (!existingSet.contains(userId)) {
                ConfidentialProcedureAccess access = new ConfidentialProcedureAccess();
                access.setProcedure(procedure);
                access.setUserId(userId);
                access.setAccessType("USER");
                access.setAssignedBy(currentUserId);
                access.setAssignedAt(Instant.now());
                accessRepository.save(access);

                shareLogService.saveShareLog(procedureId, currentUserId, userId, "SHARE");
            }
        }

        // ✅ SỬA THÀNH — fetch entity rồi delete chắc chắn
        for (ConfidentialProcedureAccess access : existingAccesses) {
            if (!newSet.contains(access.getUserId())) {
                accessRepository.delete(access);
                accessRepository.flush();
                shareLogService.saveShareLog(procedureId, currentUserId, access.getUserId(), "REVOKE");
            }
        }
    }

    // =====================================================
    // PRIVATE HELPERS
    // =====================================================
    private ConfidentialProcedure fetchProcedure(Long id) {
        return procedureRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Quy trình không tồn tại"));
    }

    private String getCurrentUserId() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Không xác định user"));
        User user = userRepository.findByEmail(email);
        if (user == null)
            throw new IdInvalidException("User không tồn tại");
        return user.getId();
    }
}
