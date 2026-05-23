package vn.system.app.modules.evaluation.domain.response;

import lombok.Data;
import vn.system.app.modules.evaluation.domain.enums.RecordStatus;
import vn.system.app.modules.evaluation.domain.enums.ScoredBy;
import vn.system.app.modules.evaluation.domain.enums.CommentType;
import vn.system.app.modules.evaluation.domain.enums.TrainingGroup;
import java.time.Instant;
import java.util.List;

@Data
public class ResEvaluationRecordDTO {
    private Long id;
    private Long periodId;
    private ResEmployeeInfo employee;
    private ResEmployeeInfo directManager;
    private ResEmployeeInfo indirectManager;
    private ResTemplateDTO template;
    private RecordStatus status;
    private Instant employeeSubmittedAt;
    private Instant managerSubmittedAt;
    private Instant approvedAt;
    private Instant completedAt;
    private Double employeeTotalScore;
    private Double managerTotalScore;
    private Double approverTotalScore;
    private String finalGrade;
    private List<ResScoreDTO> scores;
    private List<ResCommentDTO> comments;
    private List<ResTrainingPlanDTO> trainingPlans;

    @Data
    public static class ResEmployeeInfo {
        private String id;
        private String username;
        private String fullName;
        private String email;
        private String jobTitle;
        private String positionLevel;
    }

    @Data
    public static class ResScoreDTO {
        private Long id;
        private Long criteriaId;
        private ScoredBy scoredBy;
        private Double score;
        private Double weightedScore;
    }

    @Data
    public static class ResCommentDTO {
        private Long id;
        private CommentType commentType;
        private String content;
        private ResEmployeeInfo writtenBy;
        private Instant writtenAt;
    }

    @Data
    public static class ResTrainingPlanDTO {
        private Long id;
        private TrainingGroup trainingGroup;
        private String content;
        private String requirements;
        private String solution;
        private String completionTimeline;
    }
}
