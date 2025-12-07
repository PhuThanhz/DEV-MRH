package vn.system.app.modules.dowload.domain.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadResponse {
    private boolean success;

    private String name; // Ví dụ: "Ngoc Anh"
    private String userId; // Ví dụ: "@minirabit_"

    private String caption; // Mô tả bài viết
    private String folder; // Tên file video đã tải về (vd: threads_1733642000000.mp4)
    private String error; // Lỗi nếu có
}
