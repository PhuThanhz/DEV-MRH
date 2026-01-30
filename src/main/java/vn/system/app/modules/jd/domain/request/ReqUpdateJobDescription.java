package vn.system.app.modules.jd.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateJobDescription {

    @NotNull(message = "ID không được để trống")
    private Long id;

    @NotBlank(message = "Tiêu đề JD không được để trống")
    private String title;

    @NotBlank(message = "Nội dung JD không được để trống")
    private String content;
}
