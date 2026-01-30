package vn.system.app.modules.section.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateSectionDTO {

    @NotNull(message = "Id bộ phận không được để trống")
    private Long id;

    // optional
    private String name;

    // optional: 1 = active, 0 = inactive
    private Integer status;
}
