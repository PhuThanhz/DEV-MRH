package vn.system.app.modules.positionlevel.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdatePositionLevelDTO {

    private Long id;
    private String code;
    private Integer bandOrder;
}
