package vn.system.app.modules.task.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.task.domain.enums.TaskAttachmentCategory;

@Getter
@Setter
public class ResTaskAttachmentDTO {

    private Long id;
    private Long taskId;
    private String fileName;
    private String folder;
    private Long fileSize;

    private String uploadedById;
    private String uploadedByName;

    private TaskAttachmentCategory attachmentCategory;
    private boolean isResultAttachment;
    private Integer submissionRound;
    private Instant createdAt;
}
