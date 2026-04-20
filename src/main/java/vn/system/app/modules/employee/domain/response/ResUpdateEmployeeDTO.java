package vn.system.app.modules.employee.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResUpdateEmployeeDTO {

    private String id;
    private String name;
    private Boolean active;

    private Instant updatedAt;
}