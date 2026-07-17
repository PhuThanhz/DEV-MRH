package vn.system.app.modules.accountingdossier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierOutbox;
import vn.system.app.modules.accountingdossier.repository.AccountingDossierOutboxRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountingDossierOutboxScheduler {
    private static final int BATCH_SIZE = 100;

    private final AccountingDossierOutboxRepository outboxRepository;

    @Scheduled(fixedDelay = 10000) // Runs every 10 seconds
    @Transactional
    public void processOutbox() {
        List<AccountingDossierOutbox> pendingRecords = outboxRepository.findPendingToProcess(Instant.now(), PageRequest.of(0, BATCH_SIZE));
        if (pendingRecords.isEmpty()) {
            return;
        }

        log.info("Processing {} outbox records...", pendingRecords.size());
        for (AccountingDossierOutbox record : pendingRecords) {
            record.setStatus("PROCESSING");

            try {
                // Execute mock notification delivery. In production, this would send an email.
                sendMockNotification(record);
                
                record.setStatus("SENT");
                record.setErrorMessage(null);
            } catch (Exception e) {
                log.error("Failed to send outbox notification for record id {}: {}", record.getId(), e.getMessage());
                record.setStatus("FAILED");
                record.setRetryCount(record.getRetryCount() + 1);
                record.setNextRetryAt(Instant.now().plus(1, ChronoUnit.MINUTES));
                record.setErrorMessage(e.getMessage());
            }
        }
    }

    private void sendMockNotification(AccountingDossierOutbox record) {
        log.info("Mock sending notification for dossier ID: {}, event: {}, payload: {}", 
            record.getDossierId(), record.getEventType(), record.getPayload());
        // For local test / validation, if the payload contains "FAIL_DELIVERY", simulate failure
        if (record.getPayload() != null && record.getPayload().contains("FAIL_DELIVERY")) {
            throw new RuntimeException("Simulated notification service outage.");
        }
    }
}
