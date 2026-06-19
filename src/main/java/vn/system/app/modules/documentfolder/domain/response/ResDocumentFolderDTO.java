package vn.system.app.modules.documentfolder.domain.response;

import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResDocumentFolderDTO {

    private Long id;
    private String folderName;
    private Long parentId;
    private String ownerId;
    private String folderType;
    private Long companyId;
    private Long documentCount;
    private List<ResDocumentFolderDTO> children;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
