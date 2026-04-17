package vn.system.app.modules.jd.jobdescriptiontask.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqTaskDTO {

    private Long id; // ← THÊM

    private Integer orderNo;

    private String title;

    private String content;

}