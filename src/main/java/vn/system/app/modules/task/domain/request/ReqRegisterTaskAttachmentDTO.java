package vn.system.app.modules.task.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.task.domain.enums.TaskAttachmentCategory;

@Getter
@Setter
public class ReqRegisterTaskAttachmentDTO {

    @NotBlank(message = "Tên file không được để trống")
    private String fileName;

    private String folder = "documents";
    private Long fileSize = 0L;
    private TaskAttachmentCategory attachmentCategory;
}
