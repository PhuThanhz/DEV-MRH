package vn.system.app.modules.sharetoken.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import vn.system.app.common.util.QrCodeUtil;
import vn.system.app.common.config.AppProperties;
import vn.system.app.common.util.SecurityUtil;

import vn.system.app.modules.email.service.EmailService;
import vn.system.app.modules.sharetoken.domain.ProcedureShareToken;
import vn.system.app.modules.sharetoken.domain.ShareTokenAccessLog;
import vn.system.app.modules.sharetoken.domain.request.CreateShareTokenRequest;
import vn.system.app.modules.sharetoken.domain.response.ResShareTokenDTO;
import vn.system.app.modules.sharetoken.repository.ProcedureShareTokenRepository;
import vn.system.app.modules.sharetoken.repository.ShareTokenAccessLogRepository;

@Service
@RequiredArgsConstructor
public class ProcedureShareTokenService {

    private final ProcedureShareTokenRepository shareTokenRepository;
    private final ShareTokenAccessLogRepository accessLogRepository;
    private final AppProperties appProperties;
    private final EmailService emailService;

    // =====================================================
    // TẠO TOKEN + SINH QR
    // =====================================================
    public ResShareTokenDTO handleCreate(Long procedureId, CreateShareTokenRequest req) {
        String token = UUID.randomUUID().toString();

        // =====================================================
        // XỬ LÝ PIN
        // - autoGeneratePin = true → tự động sinh PIN 6 số
        // - pin tự nhập → dùng PIN người dùng nhập
        // - cả hai đều null/false → không dùng PIN
        // =====================================================
        String pin = null;

        if (Boolean.TRUE.equals(req.getAutoGeneratePin())) {
            pin = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        } else if (req.getPin() != null && !req.getPin().isBlank()) {
            pin = req.getPin().trim();
        }

        String publicUrl = appProperties.getBaseUrl() + "/public/view/" + token;
        String qrCode = QrCodeUtil.generateBase64(publicUrl);

        String createdBy = SecurityUtil.getCurrentUserLogin().orElse("system");

        ProcedureShareToken entity = ProcedureShareToken.builder()
                .procedureId(procedureId)
                .procedureType(req.getProcedureType())
                .token(token)
                .pin(pin)
                .permission(req.getPermission())
                .expiresAt(req.getExpiresAt())
                .maxAccessCount(req.getMaxAccessCount())
                .qrCode(qrCode)
                .createdBy(createdBy)
                .build();

        shareTokenRepository.save(entity);

        return toDTO(entity, true);
    }

    // =====================================================
    // DANH SÁCH TOKEN CỦA 1 QUY TRÌNH
    // =====================================================
    public List<ResShareTokenDTO> fetchByProcedure(Long procedureId, String procedureType) {
        return shareTokenRepository
                .findByProcedureIdAndProcedureType(procedureId, procedureType)
                .stream()
                .map(e -> toDTO(e, true))
                .collect(Collectors.toList());
    }

    // =====================================================
    // THU HỒI TOKEN
    // =====================================================
    public void handleRevoke(Long tokenId) {
        ProcedureShareToken entity = shareTokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy token: " + tokenId));

        entity.setIsRevoked(true);
        shareTokenRepository.save(entity);
    }

    // =====================================================
    // GỬI EMAIL CHIA SẺ TOKEN
    // =====================================================
    public void handleSendEmail(Long tokenId, String recipientEmail) {
        ProcedureShareToken entity = shareTokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy token: " + tokenId));

        if (Boolean.TRUE.equals(entity.getIsRevoked())) {
            throw new RuntimeException("Token đã bị thu hồi, không thể gửi email");
        }

        String shareUrl = appProperties.getBaseUrl() + "/public/view/" + entity.getToken();

        Map<String, Object> variables = new HashMap<>();
        variables.put("shareUrl", shareUrl);
        variables.put("pin", entity.getPin());
        variables.put("qrBase64", entity.getQrCode());
        variables.put("permission", entity.getPermission());
        variables.put("expiresAt", entity.getExpiresAt());

        emailService.sendShareTokenEmail(
                recipientEmail,
                "Chia sẻ quy trình với bạn",
                variables);
    }

    // =====================================================
    // KIỂM TRA TOKEN HỢP LỆ
    // =====================================================
    public ProcedureShareToken validateToken(String token) {
        ProcedureShareToken entity = shareTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy link chia sẻ"));

        if (Boolean.TRUE.equals(entity.getIsRevoked())) {
            throw new RuntimeException("Link đã bị thu hồi");
        }

        if (entity.getExpiresAt() != null && entity.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Link đã hết hạn");
        }

        if (entity.getMaxAccessCount() != null
                && entity.getAccessCount() >= entity.getMaxAccessCount()) {
            throw new RuntimeException("Đã đạt giới hạn truy cập");
        }

        return entity;
    }

    // =====================================================
    // GHI LOG + TĂNG ACCESS COUNT
    // =====================================================
    public void recordAccess(ProcedureShareToken entity, String ip, String userAgent) {
        entity.setAccessCount(entity.getAccessCount() + 1);
        shareTokenRepository.save(entity);

        ShareTokenAccessLog log = ShareTokenAccessLog.builder()
                .shareToken(entity)
                .ipAddress(ip)
                .userAgent(userAgent)
                .build();

        accessLogRepository.save(log);
    }

    // =====================================================
    // LỊCH SỬ TRUY CẬP CỦA 1 TOKEN
    // =====================================================
    public List<ShareTokenAccessLog> fetchAccessLogs(Long tokenId) {
        shareTokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy token: " + tokenId));

        return accessLogRepository.findByShareTokenIdOrderByAccessedAtDesc(tokenId);
    }

    // =====================================================
    // CONVERT TO DTO
    // =====================================================
    public ResShareTokenDTO toDTO(ProcedureShareToken entity, boolean includeQrCode) {
        return ResShareTokenDTO.builder()
                .id(entity.getId())
                .procedureId(entity.getProcedureId())
                .procedureType(entity.getProcedureType())
                .token(entity.getToken())
                .permission(entity.getPermission())
                .expiresAt(entity.getExpiresAt())
                .maxAccessCount(entity.getMaxAccessCount())
                .accessCount(entity.getAccessCount())
                .qrCode(includeQrCode ? entity.getQrCode() : null)
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .isRevoked(entity.getIsRevoked())
                .hasPin(entity.getPin() != null && !entity.getPin().isBlank())
                .pin(entity.getPin())
                .build();
    }
}