package vn.system.app.modules.jd.domain.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResJobDescriptionDTO {
    private Long id;
    private String title;
    private String content;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
