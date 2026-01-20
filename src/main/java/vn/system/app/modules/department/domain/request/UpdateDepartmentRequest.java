package vn.system.app.modules.department.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDepartmentRequest {

    private String name; // tên phòng ban mới
    private String englishName; // tên tiếng Anh mới

    private Integer status; // cập nhật trạng thái (1 = active, 0 = inactive)
}
