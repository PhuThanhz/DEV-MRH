package vn.system.app.modules.companyprocedure.domain.request;

import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.companyprocedure.domain.enums.ProcedureStatus;

@Getter
@Setter
public class CompanyProcedureRequest {

    // Bộ phận / team (Section đã bao gồm Department + Company)
    private Long sectionId;

    // Tên quy trình / quy định
    private String procedureName;

    // Link file PDF
    private String fileUrl;

    // Trạng thái quy trình
    private ProcedureStatus status;

    // Kế hoạch năm (ví dụ: 2026)
    private Integer planYear;

    // Ghi chú
    private String note;
}
