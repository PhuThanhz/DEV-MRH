package vn.system.app.modules.evaluation.domain.request;

import lombok.Data;
import java.util.List;
import vn.system.app.modules.evaluation.domain.enums.TrainingGroup;

@Data
public class SaveScoreRequest {
    private List<ScoreInput> scores;
    private String comment;
    private List<TrainingPlanInput> trainingPlans;

    @Data
    public static class ScoreInput {
        private Long criteriaId;
        private Double score;
    }

    @Data
    public static class TrainingPlanInput {
        private TrainingGroup trainingGroup;
        private String content;
        private String requirements;
        private String solution;
        private String completionTimeline;
    }
}
