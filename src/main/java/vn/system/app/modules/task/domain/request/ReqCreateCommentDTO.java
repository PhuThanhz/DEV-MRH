package vn.system.app.modules.task.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateCommentDTO {

    @NotBlank(message = "Nội dung bình luận không được để trống")
    private String content;

    private String attachmentUrl;
}
