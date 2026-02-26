package vn.system.app.modules.organization.domain.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResOrganizationChartDTO {

    private List<ResOrganizationChartNodeDTO> nodes;
    private List<ResOrganizationChartEdgeDTO> edges;
}