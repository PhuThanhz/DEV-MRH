package vn.system.app.modules.positionlevel.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResCreatePositionLevelDTO {

    private Long id; // ID
    private String code; // S1, S2...
    private String band; // S / M / L...
    private Integer bandOrder; // thứ tự band
    private Integer levelNumber; // cấp độ (tách từ code)

    private Instant createdAt; // thời gian tạo
    private String createdBy; // người tạo
}
