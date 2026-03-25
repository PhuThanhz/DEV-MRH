package vn.system.app.modules.confidentialprocedure.domain.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfidentialProcedureRequest {

    @NotBlank(message = "Tên quy trình không được để trống")
    private String procedureName;

    private String status;
    private Integer planYear;
    private List<String> fileUrls; // ← đổi từ String fileUrl
    private String note;

    @NotNull(message = "Phòng ban không được để trống")
    private Long departmentId;

    private Long sectionId;

    private List<Long> userIds;
    private List<Long> roleIds;
}