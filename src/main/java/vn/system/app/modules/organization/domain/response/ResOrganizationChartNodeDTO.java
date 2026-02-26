package vn.system.app.modules.organization.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResOrganizationChartNodeDTO {

    private String id; // node-<jobTitleId>
    private Long jobTitleId; // ID chức danh
    private String nameVi; // Tên chức danh
    private String levelCode; // M2, S1, E2...
    private Integer bandOrder; // Thứ tự band
    private Integer levelNumber; // Level số
}