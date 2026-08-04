package vn.system.app.modules.task.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

import vn.system.app.modules.task.domain.enums.TaskExtensionStatus;

@Getter
@Setter
public class ResTaskExtensionRequestDTO {

    private Long id;
    private Long taskId;

    private Instant currentDueDate;
    private Instant requestedDueDate;
    private String reason;
    private TaskExtensionStatus status;

    private String requestedById;
    private String requestedByName;
    private String requestedByAvatar;
    private Instant requestedAt;

    private String decidedById;
    private String decidedByName;
    private Instant decidedAt;
    private String decisionNote;
}
