package vn.system.app.modules.company.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResCompanyDTO {

    private long id;
    private String code;
    private String name;
    private String englishName;
    private Integer status;
    private Instant createdAt;
    private Instant updatedAt;
}
