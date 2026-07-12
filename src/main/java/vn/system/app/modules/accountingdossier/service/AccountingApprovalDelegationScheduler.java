package vn.system.app.modules.accountingdossier.service;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccountingApprovalDelegationScheduler {

    private final Logger log = LoggerFactory.getLogger(AccountingApprovalDelegationScheduler.class);
    private final AccountingApprovalDelegationService service;

    // Chạy mỗi giờ một lần để kiểm tra và hết hạn các ủy quyền quá hạn
    @Scheduled(cron = "0 0 * * * ?")
    public void expireOverdueDelegations() {
        log.debug("Bắt đầu tiến trình quét hết hạn các ủy quyền phê duyệt kế toán...");
        int count = service.expireOverdueDelegations();
        if (count > 0) {
            log.info("Đã quét và chuyển trạng thái EXPIRED cho {} bản ghi ủy quyền quá hạn.", count);
        }
    }
}
