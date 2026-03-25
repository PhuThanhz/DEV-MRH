package vn.system.app.modules.employeecareerpath.domain.request;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqPromoteEmployeeDTO {

    // Ngày thăng tiến thực tế, mặc định hôm nay
    private LocalDate promotedAt;

    private String note;
}