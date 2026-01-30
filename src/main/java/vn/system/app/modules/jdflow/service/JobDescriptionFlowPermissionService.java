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

    /*
     * =====================================================
     * KIỂM TRA QUYỀN DUYỆT CỦA NGƯỜI HIỆN TẠI
     * =====================================================
     */
    public void checkActorApprovePermission(User actor) throws IdInvalidException {
        if (actor.getRole() == null || actor.getRole().getPermissions() == null) {
            throw new IdInvalidException("User không có quyền duyệt");
        }

        boolean hasPermission = actor.getRole().getPermissions().stream()
                .anyMatch(p -> p.getName().equals("JD_APPROVE"));

        if (!hasPermission) {
            throw new IdInvalidException("User không có permission duyệt JD");
        }
    }

    /*
     * =====================================================
     * KIỂM TRA QUYỀN BAN HÀNH JD (ISSUE)
     * =====================================================
     */
    public void checkIssuePermission(User actor) throws IdInvalidException {
        if (actor.getRole() == null || actor.getRole().getPermissions() == null) {
            throw new IdInvalidException("User không có quyền ban hành JD");
        }

        boolean hasPermission = actor.getRole().getPermissions().stream()
                .anyMatch(p -> p.getName().equals("JD_ISSUE"));

        if (!hasPermission) {
            throw new IdInvalidException("User không có quyền ban hành JD");
        }
    }

    /*
     * =====================================================
     * VALIDATE NGƯỜI DUYỆT TIẾP theo cấp bậc
     *
     * nextUser phải:
     * 1. Có quyền duyệt
     * 2. Cấp cao hơn actor (bandOrder nhỏ hơn)
     * =====================================================
     */
    public void validateNextApprover(User actor, User nextUser) throws IdInvalidException {

        if (actor.getId().equals(nextUser.getId())) {
            throw new IdInvalidException("Không thể giao duyệt cho chính mình");
        }

        // Check quyền duyệt
        checkActorApprovePermission(nextUser);

        Integer actorLevel = actor.getHighestLevel();
        Integer nextLevel = nextUser.getHighestLevel();

        if (actorLevel == null || nextLevel == null) {
            throw new IdInvalidException("Không lấy được cấp bậc để so sánh");
        }

        // nextUser phải ở cấp cao hơn (bandOrder nhỏ hơn)
        if (nextLevel > actorLevel) {
            throw new IdInvalidException("Người duyệt tiếp theo phải có cấp cao hơn bạn");
        }
    }

    /*
     * =====================================================
     * LẤY DANH SÁCH NGƯỜI CÓ THỂ DUYỆT (CAO CẤP HƠN ACTOR)
     * =====================================================
     */
    public List<User> getApproversHigherThan(User actor) throws IdInvalidException {

        Integer actorLevel = actor.getHighestLevel();
        if (actorLevel == null) {
            throw new IdInvalidException("User không có cấp bậc hợp lệ");
        }

        return userRepo.findAll().stream()
                .filter(u -> {
                    Integer lvl = u.getHighestLevel();
                    if (lvl == null)
                        return false;

                    // chỉ lấy cấp cao hơn
                    if (lvl > actorLevel)
                        return false;

                    // phải có quyền duyệt
                    boolean hasApprove = u.getRole() != null &&
                            u.getRole().getPermissions().stream()
                                    .anyMatch(p -> p.getName().equals("JD_APPROVE"));

                    return hasApprove && !u.getId().equals(actor.getId());
                })
                .collect(Collectors.toList());
    }
}
