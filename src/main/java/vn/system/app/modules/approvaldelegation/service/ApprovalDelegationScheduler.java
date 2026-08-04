package vn.system.app.modules.approvaldelegation.service;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.modules.approvaldelegation.domain.ApprovalDelegation;
import vn.system.app.modules.approvaldelegation.domain.enums.ApprovalDelegationStatus;
import vn.system.app.modules.approvaldelegation.repository.ApprovalDelegationRepository;

@Service
public class ApprovalDelegationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ApprovalDelegationScheduler.class);

    private final ApprovalDelegationRepository repository;

    public ApprovalDelegationScheduler(ApprovalDelegationRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 0 * * * *") // Runs every hour at minute 0
    @Transactional
    public void autoExpireDelegations() {
        Instant now = Instant.now();
        List<ApprovalDelegation> expiredList = repository.findByStatusAndValidToBefore(ApprovalDelegationStatus.ACTIVE, now);

        if (!expiredList.isEmpty()) {
            for (ApprovalDelegation ad : expiredList) {
                ad.setStatus(ApprovalDelegationStatus.EXPIRED);
                ad.setUpdatedAt(now);
                ad.setUpdatedBy("system-scheduler");
            }
            repository.saveAll(expiredList);
            log.info("[ApprovalDelegationScheduler] Successfully auto-expired {} delegation records", expiredList.size());
        }
    }
}
