package vn.system.app.modules.task.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateChecklistDTO {

    @NotBlank(message = "Tiêu đề mục kiểm tra không được để trống")
    private String title;

    private String assignedUserId;

    private Integer sortOrder = 0;
}
