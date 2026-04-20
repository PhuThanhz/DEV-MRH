package vn.system.app.modules.confidentialprocedure.domain.request;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class ConfidentialProcedureRequest {

    @NotBlank(message = "Mã quy trình không được để trống")
    private String procedureCode; // ← THÊM MỚI

    @NotBlank(message = "Tên quy trình không được để trống")
    private String procedureName;
    private Instant issuedDate; // ← THÊM

    private String status;
    private Integer planYear;
    private List<String> fileUrls;
    private String note;

    @NotNull(message = "Phòng ban không được để trống")
    private Long departmentId;

    private Long sectionId;

    private List<String> userIds;
    private List<Long> roleIds;
}