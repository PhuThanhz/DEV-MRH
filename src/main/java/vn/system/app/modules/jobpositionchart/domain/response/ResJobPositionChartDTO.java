package vn.system.app.modules.jobpositionchart.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResJobPositionChartDTO {

    private Long id;

    private String name;

    private String chartType;

    private Long companyId;

    private Long departmentId;

}