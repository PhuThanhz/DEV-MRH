package vn.system.app.modules.jd.jdflow.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqIssueJdFlow {

    @NotNull(message = "jdId không được để trống")
    private Long jdId;

}