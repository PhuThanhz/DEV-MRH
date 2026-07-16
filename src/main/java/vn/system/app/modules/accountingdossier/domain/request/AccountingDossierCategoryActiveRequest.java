package vn.system.app.modules.accountingdossier.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Payload dedicated to enabling or disabling a dossier template. */
@Getter
@Setter
public class AccountingDossierCategoryActiveRequest {

    @NotNull(message = "Trạng thái hiệu lực không được để trống")
    private Boolean active;
}
