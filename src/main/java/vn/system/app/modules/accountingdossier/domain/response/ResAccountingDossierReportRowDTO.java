package vn.system.app.modules.accountingdossier.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResAccountingDossierReportRowDTO {
    private String key;
    private String label;
    private Long count;

    public ResAccountingDossierReportRowDTO(String key, String label, Long count) {
        this.key = key;
        this.label = label;
        this.count = count;
    }
}
