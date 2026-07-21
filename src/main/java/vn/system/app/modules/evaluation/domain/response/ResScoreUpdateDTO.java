package vn.system.app.modules.evaluation.domain.response;

import java.util.List;

import lombok.Data;

@Data
public class ResScoreUpdateDTO {
    private List<ResEvaluationRecordDTO.ResScoreDTO> scores;
    private ResEvaluationRecordDTO.ResScoringSummaryDTO scoringSummary;
}
