package vn.system.app.modules.sourcelink.domain.response;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class ResSourceGroupMainDTO {
    private Long id;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
    private int totalGroups;
}
