package vn.system.app.modules.task.domain.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import vn.system.app.modules.task.domain.enums.TaskParticipantRole;

@Getter
@Setter
public class ResTaskDetailDTO extends ResTaskDTO {

    private List<TaskParticipantDTO> collaborators;
    private List<TaskParticipantDTO> observers;

    private List<ResTaskChecklistDTO> checklists;
    private List<ResTaskCommentDTO> comments;
    private List<ResTaskSubmissionDTO> submissions;
    private List<ResTaskAttachmentDTO> attachments;
    private List<ResTaskExtensionRequestDTO> extensionRequests;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskParticipantDTO {
        private Long id;
        private String userId;
        private String userName;
        private String userEmail;
        private String userAvatar;
        private TaskParticipantRole role;
    }
}
