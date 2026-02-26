package vn.system.app.modules.jdflow.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;

@Service
public class JobDescriptionFlowPermissionService {

    private final UserRepository userRepo;

    public JobDescriptionFlowPermissionService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // ==============================
    // HÀM CHUNG KIỂM TRA PERMISSION
    // ==============================
    private void ensurePermission(User user, String permissionName) {
        if (user == null || user.getRole() == null || user.getRole().getPermissions() == null) {
            throw new IdInvalidException("User không hợp lệ hoặc chưa được gán quyền");
        }

        boolean hasPermission = user.getRole().getPermissions().stream()
                .anyMatch(p -> permissionName.equals(p.getName()));

        if (!hasPermission) {
            throw new IdInvalidException("User không có permission: " + permissionName);
        }
    }

    // =====================================================
    // CHECK QUYỀN GỬI JD (SUBMIT)
    // =====================================================
    public void checkSubmitPermission(User actor) {
        ensurePermission(actor, "JD_SUBMIT");
    }

    // =====================================================
    // CHECK QUYỀN DUYỆT JD (APPROVE)
    // =====================================================
    public void checkApprovePermission(User actor) {
        ensurePermission(actor, "JD_APPROVE");
    }

    // =====================================================
    // CHECK QUYỀN BAN HÀNH JD (ISSUE)
    // =====================================================
    public void checkIssuePermission(User actor) {
        ensurePermission(actor, "JD_ISSUE");
    }

    // =====================================================
    // VALIDATE SUBMIT LẦN ĐẦU
    // =====================================================
    public void validateSubmit(User actor, User nextUser) {

        if (actor == null || nextUser == null) {
            throw new IdInvalidException("User không hợp lệ");
        }

        if (actor.getId().equals(nextUser.getId())) {
            throw new IdInvalidException("Không thể gửi JD cho chính mình");
        }

        checkSubmitPermission(actor);
        checkApprovePermission(nextUser);

        Integer actorRank = actor.getRank();
        Integer nextRank = nextUser.getRank();

        if (actorRank == null || nextRank == null) {
            throw new IdInvalidException("User chưa được gán chức danh / cấp bậc");
        }

        // rank nhỏ = cấp cao
        if (nextRank >= actorRank) {
            throw new IdInvalidException("Người nhận phải có cấp bậc cao hơn người gửi");
        }
    }

    // =====================================================
    // VALIDATE DUYỆT & CHUYỂN CẤP
    // =====================================================
    public void validateApproveAndNext(User actor, User nextUser) {

        if (actor == null || nextUser == null) {
            throw new IdInvalidException("User không hợp lệ");
        }

        if (actor.getId().equals(nextUser.getId())) {
            throw new IdInvalidException("Không thể giao duyệt cho chính mình");
        }

        checkApprovePermission(actor);
        checkApprovePermission(nextUser);

        Integer actorRank = actor.getRank();
        Integer nextRank = nextUser.getRank();

        if (actorRank == null || nextRank == null) {
            throw new IdInvalidException("User chưa được gán chức danh / cấp bậc");
        }

        if (nextRank >= actorRank) {
            throw new IdInvalidException("Người duyệt tiếp theo phải có cấp bậc cao hơn");
        }
    }

    // =====================================================
    // LẤY DANH SÁCH NGƯỜI CÓ THỂ DUYỆT (CAO HƠN USER HIỆN TẠI)
    // =====================================================
    public List<User> getApproversHigherThan(User actor) {

        if (actor == null) {
            throw new IdInvalidException("User không hợp lệ");
        }

        Integer actorRank = actor.getRank();
        if (actorRank == null) {
            throw new IdInvalidException("User chưa được gán chức danh / cấp bậc");
        }

        return userRepo.findAll().stream()
                .filter(u -> {

                    if (u.getId().equals(actor.getId()))
                        return false;

                    if (u.getRole() == null || u.getRole().getPermissions() == null)
                        return false;

                    boolean hasApprove = u.getRole().getPermissions().stream()
                            .anyMatch(p -> "JD_APPROVE".equals(p.getName()));

                    if (!hasApprove)
                        return false;

                    Integer userRank = u.getRank();
                    if (userRank == null)
                        return false;

                    // rank nhỏ hơn = cấp cao hơn
                    return userRank < actorRank;
                })
                .collect(Collectors.toList());
    }
}