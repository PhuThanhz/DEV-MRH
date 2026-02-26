package vn.system.app.modules.jd.jobdescription.domain.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.jd.jobdescription.domain.JDStatus;

@Getter
@Setter
public class ResJobDescriptionDTO {

    private Long id;

    private String companyName;
    private String issueNumber;
    private Instant issueDate;
    private String pageTotal;

    private String code; // ⭐ NEW
    private String revision; // ⭐ NEW

    private String jobTitleName;
    private String departmentName;
    private String belongsTo;
    private String directManager;
    private String workWith;

    private String assignerTitle;
    private String assignerName;

    private JDStatus status;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}