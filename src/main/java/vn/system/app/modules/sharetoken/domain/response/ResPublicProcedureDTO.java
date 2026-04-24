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

    // null nếu permission = VIEW_INFO
    private List<String> fileUrls;

    // true chỉ khi permission = VIEW_ALL
    private Boolean allowDownload;

    private Integer accessCount;
    private Integer maxAccessCount;
    private Instant expiresAt;
}