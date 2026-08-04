package vn.system.app.modules.jd.jobdescriptiontask.domain.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResJobDescriptionTaskDTO {

    private Long id;
    private Long jobDescriptionId;
    private String jobDescriptionCode;
    private Integer orderNo;
    private String title;
    private List<ResJobDescriptionTaskItemDTO> items;
}
