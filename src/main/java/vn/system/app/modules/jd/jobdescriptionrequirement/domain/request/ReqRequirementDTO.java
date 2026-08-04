package vn.system.app.modules.jd.jobdescriptionrequirement.domain.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqRequirementDTO {

    private List<ReqRequirementItemDTO> items;

}
