package vn.system.app.modules.jd.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateJobDescription {

    @NotBlank(message = "Tiêu đề JD không được để trống")
    private String title;

    @NotBlank(message = "Nội dung JD không được để trống")
    private String content;
}
