package vn.system.app.modules.departmentjobtitle.domain.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResJobTitleAssignStatusDTO {

    private Long id;
    private String nameVi;
    private String nameEn;
    private String positionCode;
    private String band;
    private Integer level;
    private Integer bandOrder;
    private Integer levelNumber;

    // true = đã gán vào phòng ban này (dù là DEPARTMENT, SECTION hay COMPANY)
    private boolean assigned;

    // DEPARTMENT | SECTION | COMPANY | null (chưa gán)
    private String assignSource;

    // Danh sách phòng ban khác đang dùng chức danh này
    private List<String> usedInDepartments;

    // ✅ MỚI — frontend dùng để disable checkbox
    // true khi: chưa gán ở phòng này + không phải từ COMPANY + không phải từ
    // SECTION
    private boolean canAssign;
}