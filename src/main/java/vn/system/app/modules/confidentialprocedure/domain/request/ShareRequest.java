package vn.system.app.modules.confidentialprocedure.domain.request;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShareRequest {
    private List<String> userIds;
}