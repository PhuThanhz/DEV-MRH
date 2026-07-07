package vn.system.app.modules.accountingdossier.domain.response;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResAccountingDossierStorageSummaryDTO {
    private long total;
    private long inRetention;
    private long expired;
    private long archived;
    private long expiringSoon;
    private long pendingApproval;
    private Map<String, Long> byStatus = new LinkedHashMap<>();
    private Map<String, Long> byStorageStatus = new LinkedHashMap<>();
}
