package vn.system.app.modules.accountingdossier.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccountingDossierRetentionScheduler {

    private final AccountingDossierService service;

    @Scheduled(cron = "0 15 1 * * ?")
    public void refreshExpiredRetentionStatuses() {
        service.refreshExpiredRetentionStatuses();
    }
}
