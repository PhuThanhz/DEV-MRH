package vn.system.app.modules.jd.jdflow.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqApproveJdFlow {

    /*
     * ==========================
     * JD ID
     * ==========================
     */
    @NotNull(message = "jdId không được để trống")
    private Long jdId;

    /*
     * ==========================
     * NGƯỜI DUYỆT TIẾP
     * ==========================
     * - Nếu có giá trị → chuyển duyệt tiếp
     * - Nếu null → duyệt cuối và chuyển cho người ban hành
     */
    private Long nextUserId;

}