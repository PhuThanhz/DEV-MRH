package vn.system.app.modules.accountingdossier.domain.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResAccountingDossierBulkActionDTO {

    private String bulkActionId;
    private int total;
    private int successCount;
    private int failureCount;
    private List<Item> items = new ArrayList<>();

    @Getter
    @Setter
    public static class Item {
        private Long id;
        private boolean success;
        private String status;
        private String error;
    }
}
