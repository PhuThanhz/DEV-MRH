package vn.system.app.modules.jd.duty.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqSaveJDDutyDTO {

    @NotNull
    private Long jobDescriptionId;

    @NotBlank
    private String content;
}