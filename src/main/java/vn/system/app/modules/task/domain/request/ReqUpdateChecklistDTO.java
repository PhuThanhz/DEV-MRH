package vn.system.app.modules.task.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateChecklistDTO {

    private String title;
    private String assignedUserId;
    private Boolean isCompleted;
    private Integer sortOrder;
}
