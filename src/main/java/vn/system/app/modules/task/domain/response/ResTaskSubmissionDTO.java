package vn.system.app.modules.task.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResTaskSubmissionDTO {

    private Long id;
    private Long taskId;
    private Integer submissionRound;
    private String resultSummary;
    private String deliverables;
    private String issues;
    private String nextSteps;

    private String submittedById;
    private String submittedByName;
    private String submittedByAvatar;
    private Instant submittedAt;

    private List<ResTaskAttachmentDTO> attachments;
}
