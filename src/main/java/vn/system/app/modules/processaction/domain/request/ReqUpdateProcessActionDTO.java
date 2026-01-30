package vn.system.app.modules.processaction.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateProcessActionDTO {

    private long id;
    private String name;
    private String shortDescription;
    private String description;
    private boolean isActive;
}
