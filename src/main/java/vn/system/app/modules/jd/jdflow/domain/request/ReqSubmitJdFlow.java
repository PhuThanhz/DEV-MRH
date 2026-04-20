package vn.system.app.modules.jd.jdflow.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqSubmitJdFlow {

    @NotNull(message = "jdId không được để trống")
    private Long jdId;

    // Bỏ @NotNull — backend tự xử lý null khi REJECTED
    private String nextUserId;

    /**
     * Trường mới để phân biệt 2 hành vi khi gửi lại JD bị từ chối:
     * - false (hoặc null): Gửi lại cho người vừa từ chối (logic cũ)
     * - true: Gửi về người trước đó trong chuỗi duyệt
     */
    private Boolean returnToPrevious;

    /**
     * Comment khi gửi JD
     * - Đặc biệt quan trọng khi returnToPrevious = true (Gửi về người trước)
     * - Nếu không truyền, backend sẽ tự động lấy lý do từ chối của người trước +
     * prefix
     */
    private String comment;

}