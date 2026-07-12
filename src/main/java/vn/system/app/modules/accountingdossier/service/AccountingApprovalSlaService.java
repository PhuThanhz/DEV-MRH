package vn.system.app.modules.accountingdossier.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.modules.accountingdossier.domain.AccountingDossierApprovalStep;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierOutbox;
import vn.system.app.modules.accountingdossier.domain.enums.ApprovalStepStatus;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingApprovalSlaSummaryDTO;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierApprovalStepRepository;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierOutboxRepository;

@Service
public class AccountingApprovalSlaService {

    private final AccountingDossierApprovalStepRepository stepRepository;
    private final AccountingDossierOutboxRepository outboxRepository;

    public AccountingApprovalSlaService(
            AccountingDossierApprovalStepRepository stepRepository,
            AccountingDossierOutboxRepository outboxRepository) {
        this.stepRepository = stepRepository;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public ResAccountingApprovalSlaSummaryDTO scanOverdueSteps() {
        List<AccountingDossierApprovalStep> overdueSteps = stepRepository
                .findByStatusAndActiveTrueAndDueAtBefore(ApprovalStepStatus.CURRENT, Instant.now());
        Set<String> existingKeys = overdueSteps.isEmpty() ? Set.of() : outboxRepository.findExistingIdempotencyKeys(
                overdueSteps.stream().map(step -> "APPROVAL_STEP_" + step.getId() + "_OVERDUE_LEVEL_1").collect(Collectors.toSet()))
                .stream().collect(Collectors.toSet());
        int created = 0;
        int skipped = 0;
        List<AccountingDossierOutbox> outboxes = new java.util.ArrayList<>();
        for (AccountingDossierApprovalStep step : overdueSteps) {
            String key = "APPROVAL_STEP_" + step.getId() + "_OVERDUE_LEVEL_1";
            if (existingKeys.contains(key)) {
                skipped++;
                continue;
            }
            AccountingDossierOutbox outbox = new AccountingDossierOutbox();
            outbox.setDossierId(step.getDossier().getId());
            outbox.setEventType("APPROVAL_SLA_OVERDUE");
            outbox.setIdempotencyKey(key);
            outbox.setPayload("Approval step overdue: dossierId=" + step.getDossier().getId()
                    + ", stepId=" + step.getId()
                    + ", stepName=" + step.getStepName()
                    + ", dueAt=" + step.getDueAt());
            outbox.setStatus("PENDING");
            outboxes.add(outbox);
            created++;
        }
        if (!outboxes.isEmpty()) outboxRepository.saveAll(outboxes);
        return ResAccountingApprovalSlaSummaryDTO.builder()
                .overdueSteps(overdueSteps.size())
                .remindersCreated(created)
                .remindersSkipped(skipped)
                .build();
    }
}
