package vn.system.app.modules.processaction.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResProcessActionDTO {

    private long id;
    private String code;
    private String name;
    private String shortDescription;
    private String description;
    private boolean isActive;
}
