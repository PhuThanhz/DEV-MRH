package vn.system.app.modules.positionlevel.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdatePositionLevelDTO {

    @NotNull(message = "ID không được để trống")
    private Long id;

    private String code;
    private Integer bandOrder;
    private Integer status; // ⭐ MUST HAVE

}
