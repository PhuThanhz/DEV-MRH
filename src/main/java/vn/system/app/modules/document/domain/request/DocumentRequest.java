package vn.system.app.modules.document.domain.request;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.procedure.enums.ProcedureType;

@Getter
@Setter
public class DocumentRequest {

    @NotBlank(message = "Mã văn bản không được để trống")
    private String documentCode;
    @NotBlank(message = "Tên văn bản không được để trống")
    private String documentName;
    @NotNull(message = "Loại văn bản không được để trống")
    private Long categoryId;
    private Long departmentId;
    private Long sectionId;
    private String status;
    private Instant issuedDate;
    private List<String> fileUrls;
    private String note;
    private ProcedureType procedureType;
    private Long procedureId;
    private List<Long> departmentIds;
    private List<String> userIds;

}