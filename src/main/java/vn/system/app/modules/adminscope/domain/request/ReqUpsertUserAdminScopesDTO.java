package vn.system.app.modules.adminscope.domain.request;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpsertUserAdminScopesDTO {

    @Valid
    private List<ReqUserAdminScopeItemDTO> scopes = new ArrayList<>();
}
