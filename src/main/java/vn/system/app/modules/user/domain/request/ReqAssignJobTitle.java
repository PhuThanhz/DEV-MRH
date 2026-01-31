package vn.system.app.modules.user.domain.request;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqAssignJobTitle {
    private List<Long> jobTitleIds;
}
