package vn.system.app.modules.accountingdossier.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.accountingdossier.domain.AccountingDossier;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierAuditLog;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierAuditLogRepository;

@Service
public class DossierAuditService {

    private final AccountingDossierAuditLogRepository auditLogRepository;

    public DossierAuditService(AccountingDossierAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void writeLog(AccountingDossier dossier, String actionType, String note) {
        writeLog(dossier, actionType, note, null, null, null, null, null, null, null);
    }

    @Transactional
    public void writeLog(
            AccountingDossier dossier,
            String actionType,
            String note,
            String targetType,
            Long targetId,
            String fromStatus,
            String toStatus,
            String beforeValue,
            String afterValue) {
        writeLog(dossier, actionType, note, targetType, targetId, fromStatus, toStatus, beforeValue, afterValue, null);
    }

    @Transactional
    public void writeLog(
            AccountingDossier dossier,
            String actionType,
            String note,
            String targetType,
            Long targetId,
            String fromStatus,
            String toStatus,
            String beforeValue,
            String afterValue,
            String bulkActionId) {
        AccountingDossierAuditLog log = new AccountingDossierAuditLog();
        log.setDossier(dossier);
        log.setActionType(actionType);
        log.setNote(note);
        log.setIpAddress(resolveClientIp());
        log.setUserAgent(resolveUserAgent());
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setBeforeValue(beforeValue);
        log.setAfterValue(afterValue);
        log.setBulkActionId(bulkActionId);
        log.setActorUserId(SecurityUtil.getCurrentUserId().orElse(null));
        auditLogRepository.save(log);
    }

    private String resolveClientIp() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return null;
        }
        String forwardedFor = attrs.getRequest().getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return attrs.getRequest().getRemoteAddr();
    }

    private String resolveUserAgent() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return null;
        }
        return attrs.getRequest().getHeader("User-Agent");
    }
}
