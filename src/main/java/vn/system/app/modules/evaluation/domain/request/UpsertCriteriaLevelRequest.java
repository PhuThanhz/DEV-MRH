package vn.system.app.modules.evaluation.domain.request;

import lombok.Data;

@Data
public class UpsertCriteriaLevelRequest {
    private Integer level;
    private String description;
}
