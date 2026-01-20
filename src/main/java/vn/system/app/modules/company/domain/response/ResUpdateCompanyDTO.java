package vn.system.app.modules.company.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResUpdateCompanyDTO {

    private long id;
    private String name;
    private String englishName;
    private Instant updatedAt;
}
