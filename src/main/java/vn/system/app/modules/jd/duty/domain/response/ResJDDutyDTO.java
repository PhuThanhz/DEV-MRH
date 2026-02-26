package vn.system.app.modules.jd.duty.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResJDDutyDTO {

    private Long id;
    private Long jobDescriptionId;
    private String content;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}