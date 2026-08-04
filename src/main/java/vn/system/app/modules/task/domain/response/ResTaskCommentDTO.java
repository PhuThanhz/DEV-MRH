package vn.system.app.modules.task.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

import vn.system.app.modules.task.domain.enums.TaskCommentType;

@Getter
@Setter
public class ResTaskCommentDTO {

    private Long id;
    private Long taskId;

    private String userId;
    private String userName;
    private String userAvatar;

    private String content;
    private String attachmentUrl;
    private TaskCommentType type;
    private Integer relatedSubmissionRound;
    private Instant createdAt;
}
