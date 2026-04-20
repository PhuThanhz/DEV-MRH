package vn.system.app.modules.confidentialprocedure.domain.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResAccessDTO {
    private String userId;
    private String name;
    private String email;
    private String assignedByName;
    private Instant assignedAt;
}