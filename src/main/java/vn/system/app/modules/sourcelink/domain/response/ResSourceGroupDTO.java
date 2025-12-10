package vn.system.app.modules.sourcelink.domain.response;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class ResSourceGroupDTO {
    private Long id;
    private String name;
    private Long mainGroupId;
    private String mainGroupName;
    private Instant createdAt;
    private Instant updatedAt;
    private int totalLinks;
}
