package vn.system.app.modules.jobtitle.domain.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResJobTitleDTO {

    private Long id;
    private String nameVi;
    private String nameEn;
    private Boolean active;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    private PositionLevelInfo positionLevel;

    @Getter
    @Setter
    public static class PositionLevelInfo {
        private Long id;
        private String code;
    }
}
