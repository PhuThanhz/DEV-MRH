package vn.system.app.modules.jd.jobdescription.domain.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.jd.jobdescription.domain.JDStatus;

@Getter
@Setter
public class ResCreateJobDescriptionDTO {

    private Long id;
    private JDStatus status;
    private Instant createdAt;
    private String createdBy;
}