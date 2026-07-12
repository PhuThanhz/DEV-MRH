package vn.system.app.modules.accountingdossier.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.system.app.modules.accountingdossier.domain.response.ResAccountingApprovalSlaSummaryDTO;

@Component
@RequiredArgsConstructor
public class AccountingApprovalSlaScheduler {

    private final Logger log = LoggerFactory.getLogger(AccountingApprovalSlaScheduler.class);
    private final AccountingApprovalSlaService service;

    // Chạy mỗi 15 phút một lần để quét các bước phê duyệt quá hạn SLA
    @Scheduled(cron = "0 */15 * * * ?")
    public void scanOverdueSteps() {
        log.debug("Bắt đầu tiến trình quét hạn SLA của các bước phê duyệt...");
        ResAccountingApprovalSlaSummaryDTO summary = service.scanOverdueSteps();
        if (summary.getOverdueSteps() > 0) {
            log.info("Đã quét hoàn tất. Số bước quá hạn: {}, Số thông báo nhắc nhở đã tạo: {}, Đã bỏ qua (trùng lặp): {}",
                    summary.getOverdueSteps(), summary.getRemindersCreated(), summary.getRemindersSkipped());
        }
    }
}
