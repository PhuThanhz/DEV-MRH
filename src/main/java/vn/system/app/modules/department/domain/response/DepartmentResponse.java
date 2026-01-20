package vn.system.app.modules.department.domain.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentResponse {

    private Long id; // id phòng ban
    private String code; // mã phòng ban
    private String name; // tên phòng ban
    private String englishName; // tên tiếng Anh

    private CompanyInfo company; // thông tin công ty (id + name)

    private Integer status; // trạng thái
    private Instant createdAt; // thời gian tạo
    private Instant updatedAt; // thời gian cập nhật
    private String createdBy; // tạo bởi
    private String updatedBy; // cập nhật bởi

    // ===========================
    // NESTED CLASS CompanyInfo
    // ===========================
    @Getter
    @Setter
    public static class CompanyInfo {
        private Long id; // id công ty
        private String name; // tên công ty
    }
}
