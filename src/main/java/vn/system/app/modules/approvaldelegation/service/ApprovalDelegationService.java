package vn.system.app.modules.approvaldelegation.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.approvaldelegation.domain.ApprovalDelegation;
import vn.system.app.modules.approvaldelegation.domain.enums.ApprovalDelegationStatus;
import vn.system.app.modules.approvaldelegation.domain.request.ReqCreateApprovalDelegationDTO;
import vn.system.app.modules.approvaldelegation.domain.response.ResApprovalDelegationDTO;
import vn.system.app.modules.approvaldelegation.repository.ApprovalDelegationRepository;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;

@Service
public class ApprovalDelegationService {

    private final ApprovalDelegationRepository repository;
    private final UserRepository userRepository;

    public ApprovalDelegationService(
            ApprovalDelegationRepository repository,
            UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ResApprovalDelegationDTO create(ReqCreateApprovalDelegationDTO req) {
        String currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new IdInvalidException("Bạn chưa đăng nhập"));

        if (currentUserId.equals(req.getDelegateUserId())) {
            throw new IdInvalidException("Không thể tự ủy quyền duyệt cho chính mình");
        }

        if (req.getValidFrom() != null && req.getValidTo() != null && req.getValidFrom().isAfter(req.getValidTo())) {
            throw new IdInvalidException("Thời điểm bắt đầu ủy quyền không được sau thời điểm kết thúc");
        }

        if (req.getValidTo() != null && req.getValidTo().isBefore(Instant.now())) {
            throw new IdInvalidException("Thời gian kết thúc ủy quyền không được ở quá khứ");
        }

        User delegator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IdInvalidException("Tài khoản ủy quyền không tồn tại"));
        User delegate = userRepository.findById(req.getDelegateUserId())
                .orElseThrow(() -> new IdInvalidException("Tài khoản được ủy quyền không tồn tại"));

        if (!delegate.isActive()) {
            throw new IdInvalidException("Người được ủy quyền đã ngưng hoạt động");
        }

        // Validate reciprocal loop delegation (delegate cannot delegate back to delegator)
        String targetModule = req.getModule() != null ? req.getModule().trim().toUpperCase() : "TASK";
        boolean hasLoop = repository.canActAsDelegate(
                targetModule,
                delegate.getId(),
                delegator.getId(),
                Instant.now()
        );
        if (hasLoop) {
            throw new IdInvalidException("Không thể tạo ủy quyền vòng lặp 2 chiều giữa 2 người dùng");
        }

        // Validate overlapping delegation time intervals for same delegator & module
        Instant newFrom = req.getValidFrom() != null ? req.getValidFrom() : Instant.now();
        Instant newTo = req.getValidTo();

        List<ApprovalDelegation> existingActiveList = repository.findByDelegatorUserIdAndModuleAndStatus(
                currentUserId, targetModule, ApprovalDelegationStatus.ACTIVE);

        for (ApprovalDelegation existing : existingActiveList) {
            Instant existFrom = existing.getValidFrom() != null ? existing.getValidFrom() : Instant.EPOCH;
            Instant existTo = existing.getValidTo();

            Instant maxFrom = existFrom.isAfter(newFrom) ? existFrom : newFrom;
            Instant minTo = (existTo == null) ? newTo : (newTo == null ? existTo : (existTo.isBefore(newTo) ? existTo : newTo));

            boolean overlap = (minTo == null) || !maxFrom.isAfter(minTo);
            if (overlap) {
                throw new IdInvalidException("Khoảng thời gian ủy quyền bị trùng lặp với một ủy quyền khác đang hoạt động của bạn (ủy quyền cho "
                        + existing.getDelegateUser().getName() + ")");
            }
        }

        ApprovalDelegation delegation = new ApprovalDelegation();
        delegation.setModule(req.getModule() != null ? req.getModule().trim().toUpperCase() : "TASK");
        delegation.setDelegatorUser(delegator);
        delegation.setDelegateUser(delegate);
        delegation.setValidFrom(req.getValidFrom() != null ? req.getValidFrom() : Instant.now());
        delegation.setValidTo(req.getValidTo());
        delegation.setReason(req.getReason());
        delegation.setStatus(ApprovalDelegationStatus.ACTIVE);
        delegation.setScopeType("ALL");

        ApprovalDelegation saved = repository.save(delegation);
        return convertToResDTO(saved);
    }

    @Transactional(readOnly = true)
    public boolean canActAsDelegate(String module, String delegatorUserId, String actorUserId) {
        if (module == null || delegatorUserId == null || actorUserId == null) return false;
        if (delegatorUserId.equals(actorUserId)) return false;

        return repository.canActAsDelegate(module.trim().toUpperCase(), delegatorUserId, actorUserId, Instant.now());
    }

    @Transactional(readOnly = true)
    public List<ResApprovalDelegationDTO> fetchMyDelegations() {
        String currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new IdInvalidException("Bạn chưa đăng nhập"));

        List<ApprovalDelegation> list = repository.findByDelegatorUserId(currentUserId);
        return list.stream().map(this::convertToResDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResApprovalDelegationDTO> fetchDelegationsToMe() {
        String currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new IdInvalidException("Bạn chưa đăng nhập"));

        List<ApprovalDelegation> list = repository.findByDelegateUserId(currentUserId);
        return list.stream().map(this::convertToResDTO).collect(Collectors.toList());
    }

    @Transactional
    public void revoke(Long id) {
        String currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new IdInvalidException("Bạn chưa đăng nhập"));

        ApprovalDelegation delegation = repository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Bản ghi ủy quyền không tồn tại với ID: " + id));

        if (!delegation.getDelegatorUser().getId().equals(currentUserId)) {
            throw new IdInvalidException("Bạn không có quyền thu hồi bản ghi ủy quyền này");
        }

        delegation.setStatus(ApprovalDelegationStatus.REVOKED);
        repository.save(delegation);
    }

    public ResApprovalDelegationDTO convertToResDTO(ApprovalDelegation entity) {
        ResApprovalDelegationDTO dto = new ResApprovalDelegationDTO();
        dto.setId(entity.getId());
        dto.setModule(entity.getModule());

        if (entity.getDelegatorUser() != null) {
            dto.setDelegatorUserId(entity.getDelegatorUser().getId());
            dto.setDelegatorUserName(entity.getDelegatorUser().getName());
            dto.setDelegatorUserAvatar(entity.getDelegatorUser().getAvatar());
        }

        if (entity.getDelegateUser() != null) {
            dto.setDelegateUserId(entity.getDelegateUser().getId());
            dto.setDelegateUserName(entity.getDelegateUser().getName());
            dto.setDelegateUserAvatar(entity.getDelegateUser().getAvatar());
        }

        dto.setValidFrom(entity.getValidFrom());
        dto.setValidTo(entity.getValidTo());
        dto.setStatus(entity.getStatus());
        dto.setReason(entity.getReason());
        dto.setScopeType(entity.getScopeType());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }
}
