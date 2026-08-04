package vn.system.app.modules.task.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResTaskChecklistDTO {

    private Long id;
    private Long taskId;
    private String title;
    private boolean isCompleted;

    private String assignedUserId;
    private String assignedUserName;
    private String assignedUserAvatar;

    private String completedById;
    private String completedByName;
    private Instant completedAt;

    private Integer sortOrder;
}
