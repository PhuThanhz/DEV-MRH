package vn.system.app.modules.positionlevel.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResCreatePositionLevelDTO {

    private Long id;
    private String code;
    private String band;
    private Integer bandOrder;
    private Integer levelNumber;

    // ⭐ THÊM MỚI — thông tin công ty
    private Long companyId;
    private String companyName;

    private Instant createdAt;
    private String createdBy;
}