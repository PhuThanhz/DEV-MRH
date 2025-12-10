package vn.system.app.modules.sourcelink.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqAddLinkDTO {

    @NotBlank(message = "URL không được để trống")
    private String url;

    /**
     * Người dùng có thể dán nhiều link, mỗi link cách nhau bằng xuống dòng hoặc
     * khoảng trắng.
     * Ví dụ:
     * https://threads.net/abc
     * https://threads.net/xyz
     */
}
