package vn.system.app.modules.sourcelink.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResSourceGroupDTO {
    private Long id;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
    private int totalLinks;
}
