package vn.system.app.modules.employeecareerpath.domain.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqAssignCareerPathDTO {

    @NotNull(message = "Nhân viên không được để trống")
    private Long userId;

    @NotNull(message = "Lộ trình không được để trống")
    private Long templateId; // assign vào template nào

    @NotNull(message = "Chức danh hiện tại không được để trống")
    private Long currentCareerPathId; // chức danh hiện tại → tìm bước bắt đầu trong template

    private LocalDate startDate; // ngày bắt đầu, mặc định hôm nay

    private String note;
}