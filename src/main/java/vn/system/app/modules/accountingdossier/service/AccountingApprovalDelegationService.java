package vn.system.app.modules.accountingdossier.service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.UserScopeContext;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.common.util.error.PermissionException;
import vn.system.app.modules.accountingdossier.domain.AccountingDossier;
import vn.system.app.modules.accountingdossier.domain.AccountingApprovalDelegation;
import vn.system.app.modules.accountingdossier.domain.enums.DelegationStatus;
import vn.system.app.modules.accountingdossier.domain.request.AccountingApprovalDelegationRequest;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingApprovalDelegationDTO;
import vn.system.app.modules.accountingdossier.repository.AccountingApprovalDelegationRepository;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.userposition.repository.UserPositionRepository;

@Service
public class AccountingApprovalDelegationService {

    private final AccountingApprovalDelegationRepository delegationRepository;
    private final UserRepository userRepository;
    private final UserPositionRepository userPositionRepository;
    private final AccountingDossierNotificationService notificationService;

    public AccountingApprovalDelegationService(
            AccountingApprovalDelegationRepository delegationRepository,
            UserRepository userRepository,
            UserPositionRepository userPositionRepository,
            AccountingDossierNotificationService notificationService) {
        this.delegationRepository = delegationRepository;
        this.userRepository = userRepository;
        this.userPositionRepository = userPositionRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO list(Pageable pageable, String keyword, String status) {
        UserScopeContext.UserScope scope = UserScopeContext.get();
        if (scope == null) {
            return emptyPage(pageable);
        }
        Page<AccountingApprovalDelegation> page = delegationRepository.findVisible(scope.isSuperAdmin() || scope.isAdminLevel(), scope.isCompanyLevel(), scope.userId(),
                scope.companyIds() == null || scope.companyIds().isEmpty() ? List.of(-1L) : scope.companyIds(),
                keyword == null || keyword.isBlank() ? null : keyword.trim(), status == null || status.isBlank() ? null : status.toUpperCase(Locale.ROOT), Instant.now(), pageable);
        return toPage(page, pageable);
    }

    private ResultPaginationDTO emptyPage(Pageable pageable) { return toPage(Page.empty(pageable), pageable); }
    private ResultPaginationDTO toPage(Page<AccountingApprovalDelegation> page, Pageable pageable) {
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        result.setMeta(meta);

        java.util.Set<String> userIds = page.getContent().stream()
                .flatMap(d -> java.util.stream.Stream.of(d.getDelegatorUserId(), d.getDelegateUserId()))
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        java.util.Map<String, User> userMap = userIds.isEmpty() ? java.util.Map.of() :
                userRepository.findAllById(userIds).stream()
                        .collect(java.util.stream.Collectors.toMap(User::getId, u -> u));

        result.setResult(page.getContent().stream().map(d -> toDTO(d, userMap)).toList());
        return result;
    }

    @Transactional
    public ResAccountingApprovalDelegationDTO create(AccountingApprovalDelegationRequest req) {
        validateRequest(req);
        Long companyId = resolveCompanyId(req);
        validateDelegateCompanyMembership(req.getDelegateUserId(), companyId);
        authorizeCreate(req.getDelegatorUserId(), companyId);

        AccountingApprovalDelegation delegation = new AccountingApprovalDelegation();
        delegation.setDelegatorUserId(req.getDelegatorUserId());
        delegation.setDelegateUserId(req.getDelegateUserId());
        delegation.setCompanyId(companyId);
        delegation.setValidFrom(req.getValidFrom());
        delegation.setValidTo(req.getValidTo());
        delegation.setScopeType("COMPANY");
        delegation.setScopeRefId(companyId);
        delegation.setReason(req.getReason());
        delegation.setStatus(req.isActivateImmediately() ? DelegationStatus.ACTIVE : DelegationStatus.DRAFT);
        AccountingApprovalDelegation saved = delegationRepository.save(delegation);
        String type = saved.getStatus() == DelegationStatus.ACTIVE
                ? "ACCOUNTING_DELEGATION_ACTIVATED"
                : "ACCOUNTING_DELEGATION_CREATED";
        notificationService.notifyUsers(List.of(saved.getDelegateUserId()), type,
                String.format("Bạn được ủy quyền xử lý phê duyệt chứng từ kế toán từ %s đến %s.%s",
                        notificationService.formatInstant(saved.getValidFrom()),
                        notificationService.formatInstant(saved.getValidTo()),
                        saved.getReason() == null || saved.getReason().isBlank() ? "" : " Lý do: " + saved.getReason()),
                null);
        return toDTO(saved);
    }

    @Transactional
    public ResAccountingApprovalDelegationDTO activate(Long id) {
        AccountingApprovalDelegation delegation = fetch(id);
        authorizeManage(delegation);
        if (delegation.getStatus() != DelegationStatus.DRAFT) {
            throw new IdInvalidException("Chỉ ủy quyền ở trạng thái nháp mới có thể kích hoạt");
        }
        if (delegation.getValidTo().isBefore(Instant.now())) {
            throw new IdInvalidException("Ủy quyền đã quá hạn, không thể kích hoạt");
        }
        delegation.setStatus(DelegationStatus.ACTIVE);
        AccountingApprovalDelegation saved = delegationRepository.save(delegation);
        notificationService.notifyUsers(List.of(saved.getDelegateUserId()), "ACCOUNTING_DELEGATION_ACTIVATED",
                String.format("Ủy quyền xử lý phê duyệt chứng từ kế toán đã được kích hoạt đến %s.",
                        notificationService.formatInstant(saved.getValidTo())),
                null);
        return toDTO(saved);
    }

    @Transactional
    public ResAccountingApprovalDelegationDTO revoke(Long id) {
        AccountingApprovalDelegation delegation = fetch(id);
        authorizeManage(delegation);
        if (delegation.getStatus() != DelegationStatus.DRAFT && delegation.getStatus() != DelegationStatus.ACTIVE) {
            throw new IdInvalidException("Chỉ có thể thu hồi ủy quyền đang nháp hoặc đang hiệu lực");
        }
        delegation.setStatus(DelegationStatus.REVOKED);
        delegation.setRevokedAt(Instant.now());
        delegation.setRevokedBy(SecurityUtil.getCurrentUserLogin().orElse(""));
        AccountingApprovalDelegation saved = delegationRepository.save(delegation);
        notificationService.notifyUsers(List.of(saved.getDelegateUserId()), "ACCOUNTING_DELEGATION_REVOKED",
                "Ủy quyền xử lý phê duyệt chứng từ kế toán của bạn đã được thu hồi.",
                null);
        return toDTO(saved);
    }

    @Transactional
    public int expireOverdueDelegations() {
        List<AccountingApprovalDelegation> overdue = delegationRepository
                .findByStatusAndValidToBefore(DelegationStatus.ACTIVE, Instant.now());
        overdue.forEach(item -> item.setStatus(DelegationStatus.EXPIRED));
        delegationRepository.saveAll(overdue);
        overdue.forEach(item -> notificationService.notifyUsers(
                List.of(item.getDelegatorUserId(), item.getDelegateUserId()),
                "ACCOUNTING_DELEGATION_EXPIRED",
                "Ủy quyền xử lý phê duyệt chứng từ kế toán đã hết hạn.",
                null));
        return overdue.size();
    }

    @Transactional(readOnly = true)
    public boolean canActAsDelegate(String delegatorUserId, String delegateUserId, AccountingDossier dossier) {
        if (delegatorUserId == null || delegateUserId == null || dossier == null || dossier.getCompany() == null) {
            return false;
        }
        Instant now = Instant.now();
        return delegationRepository
                .findByDelegatorUserIdAndDelegateUserIdAndStatusAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                        delegatorUserId,
                        delegateUserId,
                        DelegationStatus.ACTIVE,
                        now,
                        now)
                .stream()
                .anyMatch(delegation -> appliesToDossier(delegation, dossier));
    }

    private void validateRequest(AccountingApprovalDelegationRequest req) {
        if (req.getDelegatorUserId().equals(req.getDelegateUserId())) {
            throw new IdInvalidException("Không được tự ủy quyền cho chính mình");
        }
        if (!userRepository.existsById(req.getDelegatorUserId())) {
            throw new IdInvalidException("Người ủy quyền không tồn tại");
        }
        if (!userRepository.existsById(req.getDelegateUserId())) {
            throw new IdInvalidException("Người nhận ủy quyền không tồn tại");
        }
        if (!req.getValidTo().isAfter(req.getValidFrom())) {
            throw new IdInvalidException("Thời gian kết thúc ủy quyền phải sau thời gian bắt đầu");
        }
        if (req.isActivateImmediately() && req.getValidTo().isBefore(Instant.now())) {
            throw new IdInvalidException("Ủy quyền đã quá hạn, không thể kích hoạt");
        }
        if (req.getReason() == null || req.getReason().isBlank()) {
            throw new IdInvalidException("Lý do ủy quyền không được để trống");
        }
        if (req.getReason().length() > 1000) {
            throw new IdInvalidException("Lý do ủy quyền không được vượt quá 1000 ký tự");
        }
        List<DelegationStatus> liveStatuses = List.of(DelegationStatus.DRAFT, DelegationStatus.ACTIVE);
        boolean reverseOverlap = delegationRepository
                .existsByDelegatorUserIdAndDelegateUserIdAndStatusInAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                        req.getDelegateUserId(), req.getDelegatorUserId(), liveStatuses, req.getValidTo(), req.getValidFrom());
        if (reverseOverlap) {
            throw new IdInvalidException("Không được tạo vòng lặp ủy quyền hai chiều");
        }
        boolean duplicateOverlap = delegationRepository
                .existsByDelegatorUserIdAndDelegateUserIdAndStatusInAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                        req.getDelegatorUserId(), req.getDelegateUserId(), liveStatuses, req.getValidTo(), req.getValidFrom());
        if (duplicateOverlap) {
            throw new IdInvalidException("Đã có ủy quyền chồng thời gian cho người nhận này");
        }
    }

    private Long resolveCompanyId(AccountingApprovalDelegationRequest req) {
        List<Long> delegatorCompanyIds = userPositionRepository.findActiveCompanyIdsByUserId(req.getDelegatorUserId());
        Long companyId = req.getCompanyId() != null
                ? req.getCompanyId()
                : delegatorCompanyIds.stream().findFirst().orElse(null);
        if (companyId == null || !delegatorCompanyIds.contains(companyId)) {
            throw new IdInvalidException("Người ủy quyền không có vị trí đang hiệu lực tại công ty đã chọn");
        }
        return companyId;
    }

    private void validateDelegateCompanyMembership(String delegateUserId, Long companyId) {
        List<Long> delegateCompanyIds = userPositionRepository.findActiveCompanyIdsByUserId(delegateUserId);
        if (delegateCompanyIds == null || !delegateCompanyIds.contains(companyId)) {
            throw new IdInvalidException("Người nhận ủy quyền không có vị trí đang hiệu lực tại công ty đã chọn");
        }
    }

    private void authorizeCreate(String delegatorUserId, Long companyId) {
        UserScopeContext.UserScope scope = requireScope();
        if (scope.isSuperAdmin() || scope.isAdminLevel()) {
            return;
        }
        if (scope.isCompanyLevel() && scope.companyIds().contains(companyId)) {
            return;
        }
        if (scope.userId().equals(delegatorUserId)) {
            return;
        }
        throw new PermissionException("Bạn chỉ có thể tạo ủy quyền cho chính mình");
    }

    private void authorizeManage(AccountingApprovalDelegation delegation) {
        UserScopeContext.UserScope scope = requireScope();
        if (scope.isSuperAdmin() || scope.isAdminLevel()
                || (scope.isCompanyLevel() && delegation.getCompanyId() != null && scope.companyIds().contains(delegation.getCompanyId()))
                || scope.userId().equals(delegation.getDelegatorUserId())) {
            return;
        }
        throw new PermissionException("Bạn không có quyền thay đổi ủy quyền này");
    }

    private UserScopeContext.UserScope requireScope() {
        UserScopeContext.UserScope scope = UserScopeContext.get();
        if (scope == null || scope.userId() == null) {
            throw new PermissionException("Không xác định được phạm vi quyền của người dùng");
        }
        return scope;
    }

    private boolean appliesToDossier(AccountingApprovalDelegation delegation, AccountingDossier dossier) {
        if (delegation.getCompanyId() == null || !delegation.getCompanyId().equals(dossier.getCompany().getId())) {
            return false;
        }
        String scopeType = delegation.getScopeType() == null ? "COMPANY" : delegation.getScopeType().toUpperCase(Locale.ROOT);
        return "COMPANY".equals(scopeType)
                || ("DEPARTMENT".equals(scopeType) && delegation.getScopeRefId() != null
                        && dossier.getDepartment() != null
                        && delegation.getScopeRefId().equals(dossier.getDepartment().getId()));
    }

    private AccountingApprovalDelegation fetch(Long id) {
        return delegationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Ủy quyền không tồn tại"));
    }

    private ResAccountingApprovalDelegationDTO toDTO(AccountingApprovalDelegation delegation) {
        return toDTO(delegation, java.util.Map.of());
    }

    private ResAccountingApprovalDelegationDTO toDTO(AccountingApprovalDelegation delegation, java.util.Map<String, User> userMap) {
        User delegator = userMap.containsKey(delegation.getDelegatorUserId()) ? userMap.get(delegation.getDelegatorUserId()) : 
                (delegation.getDelegatorUserId() != null ? userRepository.findById(delegation.getDelegatorUserId()).orElse(null) : null);
        User delegate = userMap.containsKey(delegation.getDelegateUserId()) ? userMap.get(delegation.getDelegateUserId()) : 
                (delegation.getDelegateUserId() != null ? userRepository.findById(delegation.getDelegateUserId()).orElse(null) : null);

        return ResAccountingApprovalDelegationDTO.builder()
                .id(delegation.getId())
                .delegatorUserId(delegation.getDelegatorUserId())
                .delegatorName(delegator != null ? delegator.getName() : null)
                .delegatorEmail(delegator != null ? delegator.getEmail() : null)
                .delegateUserId(delegation.getDelegateUserId())
                .delegateName(delegate != null ? delegate.getName() : null)
                .delegateEmail(delegate != null ? delegate.getEmail() : null)
                .companyId(delegation.getCompanyId())
                .validFrom(delegation.getValidFrom())
                .validTo(delegation.getValidTo())
                .scopeType(delegation.getScopeType())
                .scopeRefId(delegation.getScopeRefId())
                .reason(delegation.getReason())
                .status(delegation.getStatus())
                .createdAt(delegation.getCreatedAt())
                .revokedAt(delegation.getRevokedAt())
                .build();
    }
}
