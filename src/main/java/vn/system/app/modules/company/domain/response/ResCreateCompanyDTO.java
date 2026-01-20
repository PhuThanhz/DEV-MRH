package vn.system.app.modules.company.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResCreateCompanyDTO {

    private long id;
    private String code;
    private String name;
    private String englishName;
    private Instant createdAt;
}
