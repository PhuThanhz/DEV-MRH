package vn.system.app.modules.positionlevel.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResPositionLevelDTO {

    private Long id;
    private String code;
    private String band;
    private Integer bandOrder;
    private Integer levelNumber;

    private Integer status;
    private boolean active; // ⭐ MATCH FE

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
