package vn.system.app.modules.task.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.modules.task.domain.TaskAttachment;
import vn.system.app.modules.task.domain.TaskSubmission;
import vn.system.app.modules.task.domain.response.ResTaskSubmissionDTO;
import vn.system.app.modules.task.repository.TaskAttachmentRepository;
import vn.system.app.modules.task.repository.TaskSubmissionRepository;

@Service
public class TaskSubmissionService {

    private final TaskSubmissionRepository submissionRepository;
    private final TaskAttachmentRepository attachmentRepository;
    private final TaskAttachmentService attachmentService;

    public TaskSubmissionService(
            TaskSubmissionRepository submissionRepository,
            TaskAttachmentRepository attachmentRepository,
            TaskAttachmentService attachmentService) {
        this.submissionRepository = submissionRepository;
        this.attachmentRepository = attachmentRepository;
        this.attachmentService = attachmentService;
    }

    @Transactional(readOnly = true)
    public List<ResTaskSubmissionDTO> fetchSubmissionsByTask(Long taskId) {
        List<TaskSubmission> submissions = submissionRepository.findByTaskIdOrderBySubmissionRoundAsc(taskId);
        List<TaskAttachment> attachments = attachmentRepository.findByTaskIdOrderByCreatedAtDesc(taskId);

        Map<Integer, List<TaskAttachment>> attachmentsByRound = attachments.stream()
                .filter(TaskAttachment::isResultAttachment)
                .collect(Collectors.groupingBy(TaskAttachment::getSubmissionRound));

        return submissions.stream().map(sub -> {
            ResTaskSubmissionDTO dto = convertToResDTO(sub);
            List<TaskAttachment> roundAtts = attachmentsByRound.getOrDefault(sub.getSubmissionRound(), List.of());
            dto.setAttachments(roundAtts.stream().map(attachmentService::convertToResDTO).collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

    public ResTaskSubmissionDTO convertToResDTO(TaskSubmission sub) {
        ResTaskSubmissionDTO dto = new ResTaskSubmissionDTO();
        dto.setId(sub.getId());
        dto.setTaskId(sub.getTask().getId());
        dto.setSubmissionRound(sub.getSubmissionRound());
        dto.setResultSummary(sub.getResultSummary());
        dto.setDeliverables(sub.getDeliverables());
        dto.setIssues(sub.getIssues());
        dto.setNextSteps(sub.getNextSteps());

        if (sub.getSubmittedBy() != null) {
            dto.setSubmittedById(sub.getSubmittedBy().getId());
            dto.setSubmittedByName(sub.getSubmittedBy().getName());
            dto.setSubmittedByAvatar(sub.getSubmittedBy().getAvatar());
        }

        dto.setSubmittedAt(sub.getSubmittedAt());
        return dto;
    }
}
