package vn.system.app.modules.document.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import vn.system.app.modules.procedure.enums.ProcedureType;

@Getter
@Setter
public class ResDocumentDTO {

    private Long id;
    private String documentCode;
    private String documentName;
    private final String type = "DOCUMENT";

    private CategoryRef category;
    private DepartmentRef department;
    private SectionRef section;

    private String status;
    private Instant issuedDate;
    private List<String> fileUrls;
    private String note;
    private boolean active;
    private Integer version;

    private ProcedureType procedureType;
    private Long procedureId;

    private String qrCode;

    // Danh sách userId được xem — chỉ có khi category.mappingProcedure = false
    private List<String> userIds;
    private List<UserAccessRef> accessDetails;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @Getter
    @Setter
    public static class CategoryRef {
        private Long id;
        private String categoryCode;
        private String categoryName;
        private String symbol;
        private boolean mappingProcedure;
    }

    @Getter
    @Setter
    public static class DepartmentRef {
        private Long id;
        private String name;
        private Long companyId;
        private String companyName;
        private String companyCode;
    }

    @Getter
    @Setter
    public static class SectionRef {
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    public static class UserAccessRef {
        private String userId;
        private String userName;
        private Boolean isRead;
        private Instant readAt;
        private Instant assignedAt;
    }
}