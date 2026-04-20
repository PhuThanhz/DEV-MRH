package vn.system.app.modules.confidentialprocedure.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.confidentialprocedure.domain.ProcedureShareLog;
import vn.system.app.modules.confidentialprocedure.domain.response.ResShareLogDTO;
import vn.system.app.modules.confidentialprocedure.repository.ConfidentialProcedureAccessRepository;
import vn.system.app.modules.confidentialprocedure.repository.ConfidentialProcedureRepository;
import vn.system.app.modules.confidentialprocedure.repository.ProcedureShareLogRepository;

@Service
public class ConfidentialShareLogService {

    private final ProcedureShareLogRepository shareLogRepository;
    private final ConfidentialProcedureAccessRepository accessRepository;
    private final ConfidentialProcedureRepository procedureRepository;
    private final UserRepository userRepository;

    public ConfidentialShareLogService(
            ProcedureShareLogRepository shareLogRepository,
            ConfidentialProcedureAccessRepository accessRepository,
            ConfidentialProcedureRepository procedureRepository,
            UserRepository userRepository) {

        this.shareLogRepository = shareLogRepository;
        this.accessRepository = accessRepository;
        this.procedureRepository = procedureRepository;
        this.userRepository = userRepository;
    }

    // =====================================================
    // SAVE SHARE LOG (dùng bởi AccessService)
    // =====================================================
    @Transactional
    public void saveShareLog(Long procedureId, String senderId, String receiverId, String action) {
        ProcedureShareLog log = new ProcedureShareLog();
        log.setProcedureId(procedureId);
        log.setSenderId(senderId);
        log.setReceiverId(receiverId);
        log.setSentAt(Instant.now());
        log.setAction(action);
        shareLogRepository.save(log);
    }

    // =====================================================
    // SHARE LOG — ĐÃ GỬI
    // Chỉ hiển thị những chia sẻ ĐANG HIỆU LỰC (access vẫn còn)
    // =====================================================
    public List<ResShareLogDTO> handleGetSentLog() {
        String currentUserId = getCurrentUserId();

        return shareLogRepository.findBySenderIdOrderBySentAtDesc(currentUserId)
                .stream()
                .filter(log -> "SHARE".equals(log.getAction()) &&
                        accessRepository.existsByProcedure_IdAndUserIdAndAccessType(
                                log.getProcedureId(), log.getReceiverId(), "USER"))
                // ✅ THÊM: group theo (procedureId + receiverId), giữ cái mới nhất
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                log -> log.getProcedureId() + "-" + log.getReceiverId(),
                                log -> log,
                                (existing, replacement) -> existing // giữ cái đầu (đã sort desc = mới nhất)
                        ),
                        map -> map.values().stream()
                                .sorted((a, b) -> b.getSentAt().compareTo(a.getSentAt()))
                                .collect(Collectors.toList())))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // SHARE LOG — ĐÃ NHẬN
    // =====================================================
    // ✅ SỬA THÀNH
    public List<ResShareLogDTO> handleGetReceivedLog() {
        String currentUserId = getCurrentUserId();

        return shareLogRepository.findByReceiverIdOrderBySentAtDesc(currentUserId)
                .stream()
                .filter(log -> "SHARE".equals(log.getAction()) &&
                        accessRepository.existsByProcedure_IdAndUserIdAndAccessType(
                                log.getProcedureId(), log.getReceiverId(), "USER"))
                // ✅ group theo (procedureId + senderId), giữ log mới nhất
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                log -> log.getProcedureId() + "-" + log.getSenderId(),
                                log -> log,
                                (existing, replacement) -> existing),
                        map -> map.values().stream()
                                .sorted((a, b) -> b.getSentAt().compareTo(a.getSentAt()))
                                .collect(Collectors.toList())))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // SHARE LOG — TẤT CẢ (admin audit) — sort mới nhất trước
    // =====================================================
    public ResultPaginationDTO handleGetAllLog(Pageable pageable) {
        // Dùng findAllByOrderBySentAtDesc thay vì findAll để có sort đúng
        Page<ProcedureShareLog> page = shareLogRepository.findAllByOrderBySentAtDesc(pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);

        rs.setResult(page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));

        return rs;
    }

    // =====================================================
    // CONVERT TO DTO
    // =====================================================
    public ResShareLogDTO convertToDTO(ProcedureShareLog log) {
        ResShareLogDTO dto = new ResShareLogDTO();

        dto.setId(log.getId());
        dto.setProcedureId(log.getProcedureId());
        dto.setAction(log.getAction());
        dto.setSentAt(log.getSentAt());

        procedureRepository.findById(log.getProcedureId()).ifPresent(p -> {
            dto.setProcedureName(p.getProcedureName());
            dto.setProcedureCode(p.getProcedureCode());
            dto.setProcedureStatus(p.getStatus());
            dto.setProcedureVersion(p.getVersion());
            dto.setProcedureIssuedDate(p.getIssuedDate());

            if (p.getDepartment() != null) {
                dto.setDepartmentId(p.getDepartment().getId());
                if (p.getDepartment().getCompany() != null) {
                    dto.setCompanyId(p.getDepartment().getCompany().getId());
                }
            }
        });

        userRepository.findById(log.getSenderId()).ifPresent(u -> {
            dto.setSenderId(u.getId());
            dto.setSenderName(u.getName());
            dto.setSenderEmail(u.getEmail());
            if (u.getRole() != null)
                dto.setSenderRole(u.getRole().getName());
        });

        userRepository.findById(log.getReceiverId()).ifPresent(u -> {
            dto.setReceiverId(u.getId());
            dto.setReceiverName(u.getName());
            dto.setReceiverEmail(u.getEmail());
            if (u.getRole() != null)
                dto.setReceiverRole(u.getRole().getName());
        });

        return dto;
    }

    // =====================================================
    // PRIVATE HELPERS
    // =====================================================
    private String getCurrentUserId() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Không xác định user"));
        User user = userRepository.findByEmail(email);
        if (user == null)
            throw new IdInvalidException("User không tồn tại");
        return user.getId();
    }
}