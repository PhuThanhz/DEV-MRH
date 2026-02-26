package vn.system.app.modules.jd.positionchart.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateJDPositionChartDTO {

    @NotNull
    private Long jobDescriptionId;

    @NotBlank
    private String nodeKey;

    @NotBlank
    private String title;

    private Long parentId;

    private Integer sortOrder;
}