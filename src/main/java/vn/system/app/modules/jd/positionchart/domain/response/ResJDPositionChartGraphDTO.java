package vn.system.app.modules.jd.positionchart.domain.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResJDPositionChartGraphDTO {

    private List<ResJDPositionChartNodeDTO> nodes;
}