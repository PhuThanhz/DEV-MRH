package vn.system.app.modules.evaluation.domain.request;

import lombok.Data;
import vn.system.app.modules.evaluation.domain.enums.TemplateType;
import java.util.List;

@Data
public class CreateTemplateRequest {
    private String name;
    private TemplateType type;
    private String description;
    private List<CreateSectionRequest> sections;
}
