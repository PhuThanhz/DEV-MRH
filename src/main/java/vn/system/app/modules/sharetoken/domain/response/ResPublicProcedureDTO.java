package vn.system.app.modules.sharetoken.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResPublicProcedureDTO {

    private String procedureCode;
    private String procedureName;
    private String status;
    private Integer version;
    private Instant issuedDate;
    private String departmentName;
    private String sectionName;
    private String note;

    private List<String> fileUrls; // luôn trả về
    private Boolean allowDownload; // luôn true

    private Integer accessCount;
    private Integer maxAccessCount;
    private Instant expiresAt;
}