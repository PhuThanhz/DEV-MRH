package vn.system.app.modules.documentfolder.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentFolderRequest {

    @NotBlank(message = "Tên thư mục không được để trống")
    private String folderName;

    private Long parentId;

    private String ownerId;

    private String folderType = "PERSONAL";

    private Long companyId;
}
