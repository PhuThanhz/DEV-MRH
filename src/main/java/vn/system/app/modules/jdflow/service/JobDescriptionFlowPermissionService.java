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

    // =====================================================
    // CHECK QUYỀN GỬI JD (SUBMIT)
    // =====================================================
    public void checkSubmitPermission(User actor) {

        if (actor == null || actor.getRole() == null || actor.getRole().getPermissions() == null) {
            throw new IdInvalidException("User không có quyền gửi JD");
        }

        boolean hasPermission = actor.getRole().getPermissions().stream()
                .anyMatch(p -> "JD_SUBMIT".equals(p.getName()));

        if (!hasPermission) {
            throw new IdInvalidException("User không có permission JD_SUBMIT");
        }
    }

    // =====================================================
    // CHECK QUYỀN DUYỆT JD (APPROVE)
    // =====================================================
    public void checkApprovePermission(User actor) {

        if (actor == null || actor.getRole() == null || actor.getRole().getPermissions() == null) {
            throw new IdInvalidException("User không có quyền duyệt JD");
        }

        boolean hasPermission = actor.getRole().getPermissions().stream()
                .anyMatch(p -> "JD_APPROVE".equals(p.getName()));

        if (!hasPermission) {
            throw new IdInvalidException("User không có permission duyệt JD");
        }
    }

    // =====================================================
    // CHECK QUYỀN BAN HÀNH JD (ISSUE)
    // =====================================================
    public void checkIssuePermission(User actor) {

        if (actor == null || actor.getRole() == null || actor.getRole().getPermissions() == null) {
            throw new IdInvalidException("User không có quyền ban hành JD");
        }

        boolean hasPermission = actor.getRole().getPermissions().stream()
                .anyMatch(p -> "JD_ISSUE".equals(p.getName()));

        if (!hasPermission) {
            throw new IdInvalidException("User không có permission ban hành JD");
        }
    }

    // =====================================================
    // VALIDATE GỬI JD LẦN ĐẦU (SUBMIT)
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

        // rank nhỏ hơn = cấp cao hơn
        if (nextRank >= actorRank) {
            throw new IdInvalidException("Người nhận phải có cấp bậc cao hơn người gửi");
        }
    }

    // =====================================================
    // VALIDATE DUYỆT & CHUYỂN CẤP (APPROVE)
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
    // LẤY DANH SÁCH NGƯỜI DUYỆT CAO HƠN (CHO UI)
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
