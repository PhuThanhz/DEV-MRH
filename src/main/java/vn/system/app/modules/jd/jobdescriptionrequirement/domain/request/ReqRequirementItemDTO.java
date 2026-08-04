package vn.system.app.modules.jd.jobdescriptionrequirement.domain.request;

import lombok.Getter;
import lombok.Setter;

import vn.system.app.modules.jd.jobdescriptionrequirement.domain.enums.RequirementCategory;

@Getter
@Setter
public class ReqRequirementItemDTO {

    private Long id;

    private RequirementCategory category;

    private Integer orderNo;

    private String content;

}
