package vn.system.app.modules.accountingdossier.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import vn.system.app.modules.accountingdossier.domain.AccountingDossier;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierApprovalStep;
import vn.system.app.modules.notification.event.AppNotificationEvent;

@Service
public class AccountingDossierNotificationService {

    public static final String MODULE = "ACCOUNTING_DOSSIERS";

    private final ApplicationEventPublisher eventPublisher;

    public AccountingDossierNotificationService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void notifyUsers(Collection<String> userIds, String type, String content, Long dossierId) {
        LinkedHashSet<String> recipients = normalizeRecipients(userIds);
        if (recipients.isEmpty()) return;
        eventPublisher.publishEvent(new AppNotificationEvent(
                List.copyOf(recipients),
                MODULE,
                type,
                content,
                buildDossierLink(dossierId)));
    }

    public void notifyCreator(AccountingDossier dossier, String type, String content) {
        if (dossier == null) return;
        notifyUsers(List.of(dossier.getCreatorId()), type, content, dossier.getId());
    }

    public void notifyApprovalStep(AccountingDossier dossier, AccountingDossierApprovalStep step, String type, String content) {
        if (dossier == null || step == null) return;
        notifyUsers(resolveStepRecipientIds(step), type, content, dossier.getId());
    }

    public void notifyCreatorAndStep(AccountingDossier dossier, AccountingDossierApprovalStep step, String type, String content) {
        LinkedHashSet<String> recipients = new LinkedHashSet<>();
        if (dossier != null) {
            recipients.add(dossier.getCreatorId());
        }
        recipients.addAll(resolveStepRecipientIds(step));
        notifyUsers(recipients, type, content, dossier == null ? null : dossier.getId());
    }

    public Set<String> resolveStepRecipientIds(AccountingDossierApprovalStep step) {
        LinkedHashSet<String> recipients = new LinkedHashSet<>();
        if (step == null) return recipients;

        if (step.getApproverUserId() != null && !step.getApproverUserId().isBlank()) {
            recipients.add(step.getApproverUserId());
        }
        if (step.getEligibleApproverIds() != null && !step.getEligibleApproverIds().isBlank()) {
            Arrays.stream(step.getEligibleApproverIds().split(","))
                    .map(String::trim)
                    .filter(id -> !id.isBlank())
                    .forEach(recipients::add);
        }
        return recipients;
    }

    public String dossierLabel(AccountingDossier dossier) {
        if (dossier == null) return "bộ chứng từ";
        if (dossier.getDossierCode() != null && !dossier.getDossierCode().isBlank()) {
            return dossier.getDossierCode();
        }
        if (dossier.getContent() != null && !dossier.getContent().isBlank()) {
            return dossier.getContent();
        }
        return "bộ chứng từ #" + dossier.getId();
    }

    public String formatInstant(Instant instant) {
        if (instant == null) return "";
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(instant);
    }

    public String documentCheckStatusLabel(String status) {
        if (status == null) return "cần kiểm tra";
        return switch (status) {
            case "NEED_SUPPLEMENT" -> "cần bổ sung";
            case "INVALID" -> "không hợp lệ";
            case "VALID" -> "hợp lệ";
            case "NOT_REQUIRED" -> "không bắt buộc";
            default -> status;
        };
    }

    private LinkedHashSet<String> normalizeRecipients(Collection<String> userIds) {
        LinkedHashSet<String> recipients = new LinkedHashSet<>();
        if (userIds == null) return recipients;
        userIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(id -> !id.isBlank())
                .forEach(recipients::add);
        return recipients;
    }

    private String buildDossierLink(Long dossierId) {
        if (dossierId == null) {
            return "/admin/accounting-dossiers";
        }
        return "/admin/accounting-dossiers?dossierId=" + dossierId;
    }
}
