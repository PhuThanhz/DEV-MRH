package vn.system.app.modules.evaluation.domain.request;

import lombok.Data;
import java.util.List;

@Data
public class CreateSectionRequest {
    private String code;
    private String name;
    private Double weight;
    private Integer displayOrder;
    private List<CreateCriteriaRequest> criteria;
}
