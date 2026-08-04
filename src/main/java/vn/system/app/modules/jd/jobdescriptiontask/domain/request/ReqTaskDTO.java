package vn.system.app.modules.jd.jobdescriptiontask.domain.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqTaskDTO {

    private Long id;

    private Integer orderNo;

    private String title;

    private List<ReqJobDescriptionTaskItemDTO> items;

}
