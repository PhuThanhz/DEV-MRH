package vn.system.app.modules.careerpath.domain.response;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ResCareerPathBandGroupDTO {

    private String band;
    private Integer bandOrder;
    private List<CareerPathResponse> positions;
}