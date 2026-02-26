package vn.system.app.modules.jd.positionchart.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResJDPositionChartNodeDTO {

    private Long id;
    private String nodeKey;
    private String title;
    private Long parentId;
    private Integer sortOrder;
}