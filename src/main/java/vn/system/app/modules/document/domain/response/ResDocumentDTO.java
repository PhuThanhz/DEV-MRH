package vn.system.app.modules.document.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import vn.system.app.modules.procedure.enums.ProcedureType;

@Getter
@Setter
public class ResDocumentDTO {

    private Long id;
    private String documentCode;
    private String documentName;
    private final String type = "DOCUMENT";

    private CategoryRef category;
    private AccountingCategoryRef accountingCategory;
    private DepartmentRef department;
    private SectionRef section;

    private String status;
    private Instant issuedDate;
    private List<String> fileUrls;
    private String note;
    private boolean active;
    private Integer version;

    private Boolean isShortcut = false;

    private ProcedureType procedureType;
    private Long procedureId;

    private String qrCode;

    private FolderRef folder;

    // Danh sách userId được xem — chỉ có khi category.mappingProcedure = false
    private List<String> userIds;
    private List<Long> targetCompanyIds;
    private List<UserAccessRef> accessDetails;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Boolean isLocked;
    private Instant lockedAt;
    private String lockedBy;

    @Getter
    @Setter
    public static class CategoryRef {
        private Long id;
        private String categoryCode;
        private String categoryName;
        private String symbol;
        private boolean mappingProcedure;
        @JsonProperty("isCrossCompany")
        private boolean isCrossCompany;
    }

    @Getter
    @Setter
    public static class AccountingCategoryRef {
        private Long id;
        private String categoryCode;
        private String categoryName;
        private String symbol;
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

    @Getter
    @Setter
    public static class FolderRef {
        private Long id;
        private String folderName;
    }
}
