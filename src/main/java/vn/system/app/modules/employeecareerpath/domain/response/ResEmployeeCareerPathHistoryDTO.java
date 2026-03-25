package vn.system.app.modules.employeecareerpath.domain.response;

import java.time.Instant;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResEmployeeCareerPathHistoryDTO {

    private Long id;

    private Integer fromStepOrder;
    private String fromPositionCode;
    private String fromPositionName;

    private Integer toStepOrder;
    private String toPositionCode;
    private String toPositionName;

    private LocalDate promotedAt;
    private String note;

    private Instant createdAt;
    private String createdBy;
}