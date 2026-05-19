package vn.system.app.modules.evaluation.domain.request;

import lombok.Data;
import java.util.List;

@Data
public class CreateCriteriaRequest {
    private String name;
    private String measurementMethod;
    private Double weight;
    private Integer displayOrder;
    private Boolean hasSubCriteria;
    private List<CreateCriteriaRequest> subCriteria;
    private List<UpsertCriteriaLevelRequest> levels;
}
