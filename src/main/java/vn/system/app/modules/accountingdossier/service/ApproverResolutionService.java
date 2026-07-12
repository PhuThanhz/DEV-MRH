package vn.system.app.modules.accountingdossier.service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import vn.system.app.modules.accountingdossier.domain.enums.PositionReferenceType;
import vn.system.app.modules.accountingdossier.domain.enums.PositionResolverScope;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.userposition.repository.UserPositionRepository;

@Service
public class ApproverResolutionService {
    // Admin có full quyền nhưng không phải Giám đốc nghiệp vụ, nên không được đếm là ứng viên duyệt cuối.
    private static final Set<String> ADMIN_ROLES = Set.of("SUPER_ADMIN", "ADMIN_SUB_1", "ADMIN_SUB_2");
    private static final String ACCOUNTANT_APPROVAL_PERMISSION = "Phê duyệt bộ chứng từ kế toán - Kế toán";
    private static final String CHIEF_ACCOUNTANT_APPROVAL_PERMISSION = "Phê duyệt bộ chứng từ kế toán - Kế toán trưởng";
    private static final String DIRECTOR_APPROVAL_PERMISSION = "Phê duyệt bộ chứng từ kế toán - Giám đốc";

    private final UserRepository userRepository;
    private final UserPositionRepository userPositionRepository;

    /** Kept for legacy unit tests and callers which only resolve permission-based approvers. */
    public ApproverResolutionService(UserRepository userRepository) {
        this(userRepository, null);
    }

    @Autowired
    public ApproverResolutionService(UserRepository userRepository, UserPositionRepository userPositionRepository) {
        this.userRepository = userRepository;
        this.userPositionRepository = userPositionRepository;
    }

    public String resolveAccountantUserId(Long companyId) {
        return resolveFirstBusinessApproverId(ACCOUNTANT_APPROVAL_PERMISSION, companyId);
    }

    public String resolveChiefAccountantUserId(Long companyId) {
        return resolveFirstBusinessApproverId(CHIEF_ACCOUNTANT_APPROVAL_PERMISSION, companyId);
    }

    public List<User> resolveBusinessApproversByPermission(String permissionName, Long companyId) {
        return userRepository.findUsersByPermissionAndCompany(List.of(permissionName), List.of(companyId), true)
                .stream()
                .filter(this::isBusinessApprover)
                .sorted(Comparator.comparing(User::getId))
                .toList();
    }

    public String resolveDirectorUserId(Long companyId) {
        List<User> users = resolveAllDirectorUserIds(companyId);
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0).getId();
    }

    public List<User> resolveAllDirectorUserIds(Long companyId) {
        return userRepository.findUsersByPermissionAndCompany(
                List.of(DIRECTOR_APPROVAL_PERMISSION), List.of(companyId), true)
                .stream()
                .filter(this::isBusinessApprover)
                .sorted(Comparator.comparing(User::getId))
                .toList();
    }

    public List<User> resolvePositionApprovers(
            PositionReferenceType referenceType,
            String referenceId,
            PositionResolverScope resolverScope,
            Long companyId,
            String requesterUserId,
            Long appliedDepartmentId) {
        if (userPositionRepository == null || referenceType == null || resolverScope == null || companyId == null) {
            return List.of();
        }
        Long parsedReferenceId;
        try {
            parsedReferenceId = Long.valueOf(referenceId);
        } catch (RuntimeException ignored) {
            return List.of();
        }
        Long departmentId = null;
        if (resolverScope == PositionResolverScope.REQUESTER_DEPARTMENT) {
            departmentId = userPositionRepository.findActiveDepartmentIdsByUserId(requesterUserId).stream()
                    .sorted()
                    .findFirst()
                    .orElse(null);
        } else if (resolverScope == PositionResolverScope.APPLIED_DEPARTMENT) {
            departmentId = appliedDepartmentId;
        }
        if (resolverScope != PositionResolverScope.COMPANY && departmentId == null) {
            return List.of();
        }
        return userPositionRepository.findActiveUsersByPositionReference(
                        referenceType.name(), parsedReferenceId, resolverScope.name(), companyId, departmentId)
                .stream()
                .filter(this::isBusinessApprover)
                .sorted(Comparator.comparing(User::getId))
                .toList();
    }

    private String resolveFirstBusinessApproverId(String permissionName, Long companyId) {
        // Chọn deterministic theo id khi có nhiều người cùng quyền; Phase 2A chưa mở group queue/primary approver.
        return userRepository.findUsersByPermissionAndCompany(List.of(permissionName), List.of(companyId), true)
                .stream()
                .filter(this::isBusinessApprover)
                .sorted(Comparator.comparing(User::getId))
                .map(User::getId)
                .findFirst()
                .orElse(null);
    }

    private boolean isBusinessApprover(User user) {
        return user.getRole() == null || !ADMIN_ROLES.contains(user.getRole().getName());
    }
}
